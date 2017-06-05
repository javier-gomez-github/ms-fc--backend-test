package com.scmspain.services;

import com.scmspain.dao.TweetDao;
import com.scmspain.entities.Tweet;
import com.scmspain.entities.TweetLink;
import com.scmspain.exception.NotFoundException;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class TweetService {
    private TweetDao tweetDao;
    private MetricWriter metricWriter;
    private final static Pattern URL_PATTERN = Pattern.compile("[-a-zA-Z0-9@:%_\\+.~#?&//=]{2,256}\\.[a-z]{2,4}\\b(\\/[-a-zA-Z0-9@:%_\\+.~#?&//=]*)?");

    public TweetService(MetricWriter metricWriter, TweetDao tweetDao) {
        this.metricWriter = metricWriter;
        this.tweetDao = tweetDao;
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
        this.tweetDao.save(tweet);
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
        // reset matcher to count links again
        matcher.reset();
        // while has links
        while (matcher.find()) {
            int position = matcher.start();
            String url = matcher.group();
            TweetLink link = new TweetLink(id, url, position);
            this.tweetDao.save(link);
        }
    }

    private String getTextWithoutLinks(String text, Matcher matcher) {
        // reset matcher to count links again
        matcher.reset();
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
        Tweet tweet = this.tweetDao.getTweet(tweetId);
        if (null != tweet) {
            tweet.setDiscarded(true);
            tweet.setDiscardedDate(new Date());
            this.tweetDao.update(tweet);
        }
        else throw new NotFoundException("Tweet with id " + tweetId.toString() + " does not exist in the Database");
    }

    private Tweet buildTweetWithLinks(Tweet tweet, List<TweetLink> tweetLinkList) {
        StringBuffer stringBuffer = new StringBuffer(tweet.getTweet());
        for (TweetLink tweetLink : tweetLinkList) {
            stringBuffer.insert(tweetLink.getPosition(), tweetLink.getUrl());
        }
        tweet.setTweet(stringBuffer.toString());
        return tweet;
    }

    /**
      Recover tweets from repository
      Result - retrieved Tweets
    */
    public List<Tweet> listAllTweets() {
        List<Tweet> result = new ArrayList<Tweet>();
        this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
        List<Tweet> tweets = this.tweetDao.getTweets();
        addTweetLinks(result, tweets);
        return result;
    }

    /**
     Recover discarded tweets from repository
     Result - retrieved Discarded Tweets
     */
    public List<Tweet> listAllDiscardedTweets() {
        List<Tweet> result = new ArrayList<Tweet>();
        this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
        List<Tweet> tweets = this.tweetDao.getDiscardedTweets();
        addTweetLinks(result, tweets);
        return result;
    }

    private void addTweetLinks(List<Tweet> result, List<Tweet> tweets) {
        for (Tweet tweet : tweets) {
            List<TweetLink> tweetLinks = this.tweetDao.getTweetLinksWithQuery("FROM TweetLink WHERE tweetId = " + tweet.getId());
            result.add(!tweetLinks.isEmpty() ? buildTweetWithLinks(tweet, tweetLinks) : tweet);
        }
    }
}
