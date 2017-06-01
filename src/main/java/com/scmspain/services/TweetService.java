package com.scmspain.services;

import com.scmspain.entities.Tweet;
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

@Service
@Transactional
public class TweetService {
    private EntityManager entityManager;
    private MetricWriter metricWriter;

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
    public void publishTweet(String publisher, String text) {
        /*
         COMMENTS JGOMEZ: Extracting validations into a private method to make the code more clean
          */
        validatePublisherAndTweet(publisher, text);
        Tweet tweet = new Tweet(publisher, text);
        this.metricWriter.increment(new Delta<Number>("published-tweets", 1));
        this.entityManager.persist(tweet);
    }

    /*
     COMMENTS JGOMEZ: New method to validate the Publisher and Text and throw the corresponding Exception
      */
    private void validatePublisherAndTweet(String publisher, String text) {
        if (publisher == null || publisher.isEmpty()) {
            throw new IllegalArgumentException("Publisher must not be empty");
        }
        else if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Tweet must not be empty");
        }
        else if (text.length() > 140) {
            throw new IllegalArgumentException("Tweet must not be greater than 140 characters");
        }
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
    public Tweet getTweet(Long id) {
      return this.entityManager.find(Tweet.class, id);
    }

    /**
      Recover tweets from repository
      Result - retrieved Tweets
    */
    public List<Tweet> listAllTweets() {
        List<Tweet> result = new ArrayList<Tweet>();
        this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
        /*
         COMMENTS JGOMEZ: Changing ordering from ID to DATE to match requirements
          */
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
