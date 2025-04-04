package com.cochrane.library.cochrane.controller;

import com.cochrane.library.cochrane.config.CrawlerConfig;
import com.cochrane.library.cochrane.model.CochraneReview;
import com.cochrane.library.cochrane.model.CrawlerStatus;
import com.cochrane.library.cochrane.repository.CochraneReviewRepository;
import com.cochrane.library.cochrane.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final CrawlerConfig config;
    private final CochraneReviewRepository repository;

    @Autowired
    public CrawlerController(CrawlerService crawlerService, CrawlerConfig config, CochraneReviewRepository repository) {
        this.crawlerService = crawlerService;
        this.config = config;
        this.repository = repository;
    }

    @PostMapping("/start")
    public ResponseEntity<CrawlerStatus> startCrawl() {
        try {
            crawlerService.startCrawler();
            return ResponseEntity.ok(crawlerService.getStatus());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<CrawlerStatus> stopCrawl() {
        try {
            crawlerService.stopCrawler();
            return ResponseEntity.ok(crawlerService.getStatus());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/config")
    public ResponseEntity<CrawlerConfig> getConfig() {
        return ResponseEntity.ok(config);
    }

    @PutMapping("/config")
    public ResponseEntity<CrawlerConfig> updateConfig(@RequestBody CrawlerConfig newConfig) {
        // Update only the non-null values
        if (newConfig.getUserAgent() != null)
            config.setUserAgent(newConfig.getUserAgent());
        if (newConfig.getCronSchedule() != null)
            config.setCronSchedule(newConfig.getCronSchedule());
        config.setRequestTimeout(newConfig.getRequestTimeout());
        config.setMaxRetries(newConfig.getMaxRetries());
        config.setRetryDelay(newConfig.getRetryDelay());
        config.setDelayBetweenRequests(newConfig.getDelayBetweenRequests());
        config.setClearBeforeCrawl(newConfig.isClearBeforeCrawl());
        config.setAutoSchedule(newConfig.isAutoSchedule());

        return ResponseEntity.ok(config);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalReviews = repository.count();
        List<String> topics = repository.findAll().stream()
                .map(CochraneReview::getTopic)
                .distinct()
                .toList();

        stats.put("totalReviews", totalReviews);
        stats.put("uniqueTopics", topics.size());
        stats.put("topics", topics);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/status")
    public ResponseEntity<CrawlerStatus> getStatus() {
        return ResponseEntity.ok(crawlerService.getStatus());
    }
}