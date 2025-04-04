package com.cochrane.library.cochrane.model;

import java.time.LocalDateTime;
import java.util.List;

public class CrawlerStatus {
    private boolean isRunning;
    private LocalDateTime lastRun;
    private int totalProcessed;
    private List<CochraneReview> newReviews;
    private double crawlingSpeed;
    private String currentTopic;
    private String currentReview;
    private String errorLog;
    private String processedTopics;
    private int successfulReviews;
    private int failedReviews;

    public CrawlerStatus() {
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public LocalDateTime getLastRun() {
        return lastRun;
    }

    public void setLastRun(LocalDateTime lastRun) {
        this.lastRun = lastRun;
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public List<CochraneReview> getNewReviews() {
        return newReviews;
    }

    public void setNewReviews(List<CochraneReview> newReviews) {
        this.newReviews = newReviews;
    }

    public double getCrawlingSpeed() {
        return crawlingSpeed;
    }

    public void setCrawlingSpeed(double crawlingSpeed) {
        this.crawlingSpeed = crawlingSpeed;
    }

    public String getCurrentTopic() {
        return currentTopic;
    }

    public void setCurrentTopic(String currentTopic) {
        this.currentTopic = currentTopic;
    }

    public String getCurrentReview() {
        return currentReview;
    }

    public void setCurrentReview(String currentReview) {
        this.currentReview = currentReview;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    public String getProcessedTopics() {
        return processedTopics;
    }

    public void setProcessedTopics(String processedTopics) {
        this.processedTopics = processedTopics;
    }

    public int getSuccessfulReviews() {
        return successfulReviews;
    }

    public void setSuccessfulReviews(int successfulReviews) {
        this.successfulReviews = successfulReviews;
    }

    public int getFailedReviews() {
        return failedReviews;
    }

    public void setFailedReviews(int failedReviews) {
        this.failedReviews = failedReviews;
    }
}