package com.scmspain.dao;

import com.scmspain.entities.Tweet;
import com.scmspain.entities.TweetLink;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class TweetDao {
    private EntityManager entityManager;

    public TweetDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(Object object) {
        persist(object);
    }

    public void update(Object object) {
        persist(object);
    }

    public Tweet getTweet(Long tweetId) {
        return this.entityManager.find(Tweet.class, tweetId);
    }

    public List<Tweet> getTweets() {
        TypedQuery<Tweet> typedQuery = this.entityManager.createQuery("FROM Tweet AS tweetId WHERE pre2015MigrationStatus<>99 AND discarded = false ORDER BY date DESC", Tweet.class);
        return typedQuery.getResultList();
    }

    public List<Tweet> getDiscardedTweets() {
        TypedQuery<Tweet> typedQuery = this.entityManager.createQuery("FROM Tweet AS tweetId WHERE pre2015MigrationStatus<>99 AND discarded = true ORDER BY discardedDate DESC", Tweet.class);
        return typedQuery.getResultList();
    }

    public List<TweetLink> getTweetLinksWithQuery(String query) {
        // evict all loaded instances before getting the Tweet Links
        this.entityManager.clear();
        TypedQuery<TweetLink> typedQuery = this.entityManager.createQuery(query, TweetLink.class);
        return typedQuery.getResultList();
    }

    private void persist(Object object) {
        this.entityManager.persist(object);
        this.entityManager.flush();
    }
}
