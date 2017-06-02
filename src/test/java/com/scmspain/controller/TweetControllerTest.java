package com.scmspain.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scmspain.configuration.TestConfiguration;
import com.scmspain.entities.Tweet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
public class TweetControllerTest {
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(this.context).build();
    }

    @Test
    public void shouldReturn201WhenInsertingAValidTweet() throws Exception {
        mockMvc.perform(newTweet("Prospect", "Breaking the law"))
            .andExpect(status().is(201));
    }

    @Test
    public void shouldReturn201WhenInsertingAValidTweetWithMultipleLinks() throws Exception {
        String id = mockMvc.perform(newTweet("Publisher", "First link is: https://github.com/javier-gomez-github/ms-fc--backend-test/commit/fcb68b783ac939fe5d41528cd2f81237e3cc112b12121212212111111111212121212121 and the second is: https://github.com/javier-gomez-github/ms-fc--backend-test/commit/fcb68b783ac939fe5d41528cd2f81237e3cc112b1212121221211111111121212121212122"))
                .andExpect(status().is(201)).andReturn().getResponse().getContentAsString();

        String tweetsAsString = mockMvc.perform(get("/tweet", Long.valueOf(id))).andExpect(status().is(200))
                .andReturn().getResponse().getContentAsString();
        // using TypeReference (Jackson) to parse the result into a list of Tweet objects
        List<Tweet> tweets = new ObjectMapper().readValue(tweetsAsString, new TypeReference<List<Tweet>>(){});
        assertThat(tweets.get(0).getTweet()).isEqualTo("First link is: https://github.com/javier-gomez-github/ms-fc--backend-test/commit/fcb68b783ac939fe5d41528cd2f81237e3cc112b12121212212111111111212121212121 and the second is: https://github.com/javier-gomez-github/ms-fc--backend-test/commit/fcb68b783ac939fe5d41528cd2f81237e3cc112b1212121221211111111121212121212122");
    }

    @Test
    public void shouldReturn400WhenInsertingAnInvalidTweet() throws Exception {
        mockMvc.perform(newTweet("Schibsted Spain", "We are Schibsted Spain (look at our home page http://www.schibsted.es/), we own Vibbo, InfoJobs, fotocasa, coches.net and milanuncios. Welcome! New text to complete the 140 characters (without the Link)"))
                .andExpect(status().is(400));
    }

    @Test
    public void shouldReturn200WhenDiscardingATweet() throws Exception {
        String response = mockMvc.perform(newTweet("Prospect", "Breaking the law"))
                .andExpect(status().is(201)).andReturn().getResponse().getContentAsString();
        mockMvc.perform(discardTweet(Long.valueOf(response)))
                .andExpect(status().is(200));
    }

    @Test
    public void shouldReturn404WhenDiscardingAnUnexistingTweet() throws Exception {
        mockMvc.perform(discardTweet(999L))
                .andExpect(status().is(404));
    }

    @Test
    public void shouldReturnAllPublishedTweets() throws Exception {
        mockMvc.perform(newTweet("Yo", "How are you?"))
                .andExpect(status().is(201));

        MvcResult getResult = mockMvc.perform(get("/tweet"))
                .andExpect(status().is(200))
                .andReturn();

        String content = getResult.getResponse().getContentAsString();
        assertThat(new ObjectMapper().readValue(content, List.class).size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnAllDiscardedTweets() throws Exception {
        String id1 = mockMvc.perform(newTweet("Tweet1", "How are you?"))
                .andExpect(status().is(201)).andReturn().getResponse().getContentAsString();
        mockMvc.perform(newTweet("Tweet2", "How are you?"))
                .andExpect(status().is(201));

        // discard tweet 1
        mockMvc.perform(discardTweet(Long.valueOf(id1))).andExpect(status().is(200));

        // get discarded tweets (ordered by date)
        MvcResult getResult = mockMvc.perform(get("/discarded"))
                .andExpect(status().is(200))
                .andReturn();
        String content = getResult.getResponse().getContentAsString();
        // using TypeReference (Jackson) to parse the result into a list of Tweet objects
        List<Tweet> result = new ObjectMapper().readValue(content, new TypeReference<List<Tweet>>(){});
        assertThat(result.get(0).getId()).isEqualTo(Long.valueOf(id1));
    }

    private MockHttpServletRequestBuilder newTweet(String publisher, String tweet) {
        return post("/tweet")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(format("{\"publisher\": \"%s\", \"tweet\": \"%s\"}", publisher, tweet));
    }

    private MockHttpServletRequestBuilder discardTweet(Long id) {
        return post("/discarded")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(format("{\"tweet\": \"%s\"}", id));
    }
}
