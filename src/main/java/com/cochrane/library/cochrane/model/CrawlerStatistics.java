package com.cochrane.library.cochrane.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crawler_statistics")
public class CrawlerStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime lastUpdateTime;
    private boolean isRunning;

    private int totalProcessed;
    private int successfulReviews;
    private int failedReviews;
    private int totalTopics;
    private int processedTopics;

    @Column(length = 1000)
    private String currentTopic;

    @Column(length = 1000)
    private String currentReview;

    @Column(length = 1000)
    private String lastError;

    @Column(length = 1000)
    private String lastProcessedUrl;

    private double crawlingSpeed; // reviews per minute

    @Column(columnDefinition = "TEXT")
    private String processedTopicsList; // Comma-separated list of processed topics

    @Column(columnDefinition = "TEXT")
    private String errorLog; // Store last N errors

    public void addError(String error) {
        // Keep last 10 errors, separated by newlines
        String[] errors = (errorLog != null ? errorLog : "").split("\n");
        StringBuilder newLog = new StringBuilder();
        for (int i = Math.max(0, errors.length - 9); i < errors.length; i++) {
            newLog.append(errors[i]).append("\n");
        }
        newLog.append(LocalDateTime.now()).append(": ").append(error);
        this.errorLog = newLog.toString();
    }

    public void addProcessedTopic(String topic) {
        if (processedTopicsList == null) {
            processedTopicsList = topic;
        } else {
            processedTopicsList += "," + topic;
        }
        processedTopics++;
    }

    public void updateCrawlingSpeed() {
        if (startTime != null) {
            long minutesElapsed = java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
            if (minutesElapsed > 0) {
                this.crawlingSpeed = (double) totalProcessed / minutesElapsed;
            }
        }
    }
}