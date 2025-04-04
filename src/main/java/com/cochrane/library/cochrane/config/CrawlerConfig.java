package com.cochrane.library.cochrane.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "crawler")
public class CrawlerConfig {
    private int requestTimeout = 10000; // milliseconds
    private int maxRetries = 3;
    private long retryDelay = 1000; // milliseconds
    private long delayBetweenRequests = 500; // milliseconds
    private String userAgent = "Mozilla/5.0 (compatible; CochraneCrawler/1.0)";
    private String cronSchedule = "0 0 0 * * *"; // Daily at midnight
    private boolean clearBeforeCrawl = true;
    private boolean autoSchedule = true;
    private String baseUrl;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public long getDelayBetweenRequests() {
        return delayBetweenRequests;
    }

    public void setDelayBetweenRequests(long delayBetweenRequests) {
        this.delayBetweenRequests = delayBetweenRequests;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}