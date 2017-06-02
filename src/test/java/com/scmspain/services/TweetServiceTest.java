package com.scmspain.services;

import com.scmspain.entities.Tweet;
import com.scmspain.entities.TweetLink;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;

import javax.persistence.EntityManager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TweetServiceTest {
    private EntityManager entityManager;
    private MetricWriter metricWriter;
    private TweetService tweetService;

    @Before
    public void setUp() throws Exception {
        this.entityManager = mock(EntityManager.class);
        this.metricWriter = mock(MetricWriter.class);

        this.tweetService = new TweetService(entityManager, metricWriter);
    }

    @Test
    public void shouldInsertANewTweet() throws Exception {
        tweetService.publishTweet("Guybrush Threepwood", "I am Guybrush Threepwood, mighty pirate.");

        verify(entityManager).persist(any(Tweet.class));
    }

    @Test
    public void shouldInsertANewTweetWithLinks() throws Exception {
        tweetService.publishTweet("Publisher", "First link is: https://github.com/javier-gomez-github/ms-fc--backend-test/commit/fcb68b783ac939fe5d41528cd2f81237e3cc112b12121212212111111111212121212121 and the second is: https://github.com/javier-gomez-github/ms-fc--backend-test/commit/fcb68b783ac939fe5d41528cd2f81237e3cc112b1212121221211111111121212121212122");

        verify(entityManager, times(3)).persist(any(Tweet.class));
        verify(entityManager, atLeast(2)).persist(any(TweetLink.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenPublisherIsNull() throws Exception {
        tweetService.publishTweet(null, "Text");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenPublisherIsEmpty() throws Exception {
        tweetService.publishTweet("", "Text");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenTweetIsNull() throws Exception {
        tweetService.publishTweet("Publisher", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenTweetIsEmpty() throws Exception {
        tweetService.publishTweet("Publisher", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenTweetLengthIsInvalid() throws Exception {
        tweetService.publishTweet("Pirate", "LeChuck? He's the guy that went to the Governor's for dinner and never wanted to leave. He fell for her in a big way, but she told him to drop dead. So he did. Then things really got ugly.");
    }
}
