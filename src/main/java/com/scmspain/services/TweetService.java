package com.scmspain.services;

import com.scmspain.entities.Tweet;
import com.scmspain.entities.TweetLink;
import com.scmspain.exception.NotFoundException;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class TweetService {
    private EntityManager entityManager;
    private MetricWriter metricWriter;
    private final static Pattern URL_PATTERN = Pattern.compile("\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" +
            "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
            "|mil|biz|info|mobi|name|aero|jobs|museum" +
            "|travel|[a-z]{2}))(:[\\d]{1,5})?" +
            "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
            "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
            "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
            "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");

    public TweetService(EntityManager entityManager, MetricWriter metricWriter) {
        this.entityManager = entityManager;
        this.metricWriter = metricWriter;
    }

    /**
      Push tweet to repository
      Parameter - publisher - creator of the Tweet
      Parameter - text - Content of the Tweet
      Result - recovered Tweet
    */
    public Long publishTweet(String publisher, String text) {
        validatePublisherAndTweetNotNull(publisher, text);
        final Matcher matcher = URL_PATTERN.matcher(text);
        text = getTextWithoutLinks(text, matcher);

        if (text.length() > 140) {
            throw new IllegalArgumentException("Tweet must not be greater than 140 characters");
        }

        Tweet tweet = new Tweet(publisher, text);
        this.metricWriter.increment(new Delta<Number>("published-tweets", 1));
        this.entityManager.persist(tweet);
        this.entityManager.flush();
        // store links
        saveLinks(tweet.getId(), matcher);
        return tweet.getId();
    }

    private void validatePublisherAndTweetNotNull(String publisher, String text) {
        if (publisher == null || publisher.isEmpty()) {
            throw new IllegalArgumentException("Publisher must not be empty");
        }
        else if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Tweet must not be empty");
        }
    }

    private void saveLinks(Long id, Matcher matcher) {
        matcher.reset();
        // while has links
        while (matcher.find()) {
            int position = matcher.start();
            String url = matcher.group();
            TweetLink link = new TweetLink(id, url, position);
            entityManager.persist(link);
        }
    }

    private String getTextWithoutLinks(String text, Matcher matcher) {
        matcher.reset();
        // text has links?
        final StringBuffer textWithoutLinks = new StringBuffer(text);
        while (matcher.find()) {
            String url = matcher.group();
            textWithoutLinks.delete(textWithoutLinks.indexOf(url), (textWithoutLinks.indexOf(url) + url.length()));
        }
        return textWithoutLinks.toString();
    }

    /**
     Discard tweet from repository
     Parameter - tweetId - the Id of the Tweet
     */
    public void discardTweet(Long tweetId) {
        Tweet tweet = this.entityManager.find(Tweet.class, tweetId);
        if (null != tweet) {
            tweet.setDiscarded(true);
            tweet.setDiscardedDate(new Date());
            this.entityManager.persist(tweet);
        }
        else throw new NotFoundException("Tweet with id " + tweetId.toString() + " does not exist in the Database");
    }

    /**
      Recover tweet from repository
      Parameter - id - id of the Tweet to retrieve
      Result - retrieved Tweet
    */
    public Tweet getTweet(Long tweetIdProvided) {
        Tweet tweet = this.entityManager.find(Tweet.class, tweetIdProvided);
        List<TweetLink> tweetLinkList = new ArrayList<>();
        TypedQuery<Long> query = this.entityManager.createQuery("SELECT id FROM TweetLink WHERE tweetId = " + tweetIdProvided, Long.class);
        List<Long> tweetLinkIds = query.getResultList();
        for (Long tweetLinkId : tweetLinkIds) {
            tweetLinkList.add(getTweetLink(tweetLinkId));
        }
        return !tweetLinkList.isEmpty() ? buildTweetWithLinks(tweet, tweetLinkList) : tweet;
    }

    private Tweet buildTweetWithLinks(Tweet tweet, List<TweetLink> tweetLinkList) {
        StringBuffer stringBuffer = new StringBuffer(tweet.getTweet());
        for (TweetLink tweetLink : tweetLinkList) {
            stringBuffer.insert(tweetLink.getPosition(), tweetLink.getUrl());
        }
        tweet.setRawTextWithLinks(stringBuffer.toString());
        return tweet;
    }

    /**
     Recover tweet link from repository
     Parameter - id - id of the Tweet link to retrieve
     Result - retrieved Tweet Link
     */
    public TweetLink getTweetLink(Long id) {
        return this.entityManager.find(TweetLink.class, id);
    }

    /**
      Recover tweets from repository
      Result - retrieved Tweets
    */
    public List<Tweet> listAllTweets() {
        List<Tweet> result = new ArrayList<Tweet>();
        this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
        TypedQuery<Long> query = this.entityManager.createQuery("SELECT id FROM Tweet AS tweetId WHERE pre2015MigrationStatus<>99 AND discarded = false ORDER BY date DESC", Long.class);
        List<Long> ids = query.getResultList();
        for (Long id : ids) {
            result.add(getTweet(id));
        }
        return result;
    }

    /**
     Recover discarded tweets from repository
     Result - retrieved Discarded Tweets
     */
    public List<Tweet> listAllDiscardedTweets() {
        List<Tweet> result = new ArrayList<Tweet>();
        this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
        TypedQuery<Long> query = this.entityManager.createQuery("SELECT id FROM Tweet AS tweetId WHERE pre2015MigrationStatus<>99 AND discarded = true ORDER BY discardedDate DESC", Long.class);
        List<Long> ids = query.getResultList();
        for (Long id : ids) {
            result.add(getTweet(id));
        }
        return result;
    }
}
