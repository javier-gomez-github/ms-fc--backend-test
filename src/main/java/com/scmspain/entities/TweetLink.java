package com.scmspain.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TweetLink {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private Long tweetId;
    @Column(nullable = false)
    private String url;
    @Column (nullable=false)
    private int position;

    public TweetLink() {
    }

    public TweetLink(Long tweetId, String url, int position) {
        this.tweetId = tweetId;
        this.url = url;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTweetId() {
        return tweetId;
    }

    public void setTweetId(Long tweetId) {
        this.tweetId = tweetId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        TweetLink tweetLink = (TweetLink) o;
//
//        if (position != tweetLink.position) return false;
//        if (id != null ? !id.equals(tweetLink.id) : tweetLink.id != null) return false;
//        if (tweetId != null ? !tweetId.equals(tweetLink.tweetId) : tweetLink.tweetId != null) return false;
//        return url != null ? url.equals(tweetLink.url) : tweetLink.url == null;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = id != null ? id.hashCode() : 0;
//        result = 31 * result + (tweetId != null ? tweetId.hashCode() : 0);
//        result = 31 * result + (url != null ? url.hashCode() : 0);
//        result = 31 * result + position;
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "TweetLink{" +
//                "id=" + id +
//                ", tweetId=" + tweetId +
//                ", url='" + url + '\'' +
//                ", position=" + position +
//                '}';
//    }
}