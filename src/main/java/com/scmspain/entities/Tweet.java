package com.scmspain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public class Tweet {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String publisher;
    @Column(nullable = false, length = 140)
    private String tweet;
    @Column (nullable=true)
    private Long pre2015MigrationStatus = 0L;
    @JsonIgnore
    @Column (nullable=false)
    private Date date;
    @JsonIgnore
    @Column (nullable=false)
    private boolean discarded = false;
    @JsonIgnore
    @Column (nullable=true)
    private Date discardedDate;

    public Tweet() {
    }

    public Tweet(String publisher, String text) {
        this.publisher = publisher;
        this.tweet = text;
        this.date = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public Long getPre2015MigrationStatus() {
        return pre2015MigrationStatus;
    }

    public void setPre2015MigrationStatus(Long pre2015MigrationStatus) {
        this.pre2015MigrationStatus = pre2015MigrationStatus;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public void setDiscarded(boolean discarded) {
        this.discarded = discarded;
    }

    public Date getDiscardedDate() {
        return discardedDate;
    }

    public void setDiscardedDate(Date discardedDate) {
        this.discardedDate = discardedDate;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Tweet tweet1 = (Tweet) o;
//
//        if (id != null ? !id.equals(tweet1.id) : tweet1.id != null) return false;
//        if (publisher != null ? !publisher.equals(tweet1.publisher) : tweet1.publisher != null) return false;
//        if (tweet != null ? !tweet.equals(tweet1.tweet) : tweet1.tweet != null) return false;
//        if (pre2015MigrationStatus != null ? !pre2015MigrationStatus.equals(tweet1.pre2015MigrationStatus) : tweet1.pre2015MigrationStatus != null) {
//            return false;
//        }
//        if (date != null ? !date.equals(tweet1.date) : tweet1.date != null) return false;
//        if (discarded != tweet1.discarded) return false;
//        return discardedDate != null ? discardedDate.equals(tweet1.discardedDate) : tweet1.discardedDate == null;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = id != null ? id.hashCode() : 0;
//        result = 31 * result + (publisher != null ? publisher.hashCode() : 0);
//        result = 31 * result + (tweet != null ? tweet.hashCode() : 0);
//        result = 31 * result + (pre2015MigrationStatus != null ? pre2015MigrationStatus.hashCode() : 0);
//        result = 31 * result + (date != null ? date.hashCode() : 0);
//        result = 31 * result + (discarded ? 1 : 0);
//        result = 31 * result + (discardedDate != null ? discardedDate.hashCode() : 0);
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "Tweet{" +
//                "id=" + id +
//                ", publisher='" + publisher + '\'' +
//                ", tweet='" + tweet + '\'' +
//                ", pre2015MigrationStatus=" + pre2015MigrationStatus +
//                ", date=" + date +
//                ", discarded=" + discarded +
//                ", discardedDate=" + discardedDate +
//                '}';
//    }
}
