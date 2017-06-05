package com.scmspain.configuration;

import com.scmspain.controller.TweetController;
import com.scmspain.dao.TweetDao;
import com.scmspain.services.TweetService;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class TweetConfiguration {
    @Bean
    public TweetService getTweetService(MetricWriter metricWriter, TweetDao tweetDao) {
        return new TweetService(metricWriter, tweetDao);
    }

    @Bean
    public TweetController getTweetController(TweetService tweetService) {
        return new TweetController(tweetService);
    }

    @Bean
    public TweetDao getTweetDAO(EntityManager entityManager) {
        return new TweetDao(entityManager);
    }
}
