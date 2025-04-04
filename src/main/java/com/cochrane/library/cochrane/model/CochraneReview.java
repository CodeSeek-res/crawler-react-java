package com.cochrane.library.cochrane.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDate;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "cochrane_reviews")
public class CochraneReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, unique = true)
    private String url;

    private String topic;

    private String title;

    @Column(length = 1000)
    private String authors;

    private LocalDate publicationDate;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "last_updated")
    private LocalDate lastUpdated;

    @Column(name = "crawl_status")
    @Enumerated(EnumType.STRING)
    private CrawlStatus crawlStatus = CrawlStatus.PENDING;

    public enum CrawlStatus {
        PENDING,
        COMPLETED,
        FAILED
    }

    public CochraneReview() {
        this.lastUpdated = LocalDate.now();
    }

    public CochraneReview(String url, String topic, String title, String authors, LocalDate publicationDate) {
        this();
        this.url = url;
        this.topic = topic;
        this.title = title;
        this.authors = authors;
        this.publicationDate = publicationDate;
    }

    public void setContent(String content) {
        this.content = content;
        this.lastUpdated = LocalDate.now();
        this.crawlStatus = CrawlStatus.COMPLETED;
    }

    public void markContentFailed() {
        this.crawlStatus = CrawlStatus.FAILED;
        this.lastUpdated = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public CrawlStatus getCrawlStatus() {
        return crawlStatus;
    }

    public void setCrawlStatus(CrawlStatus crawlStatus) {
        this.crawlStatus = crawlStatus;
    }
}