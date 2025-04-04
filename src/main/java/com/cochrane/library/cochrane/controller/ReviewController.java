package com.cochrane.library.cochrane.controller;

import com.cochrane.library.cochrane.model.CochraneReview;
import com.cochrane.library.cochrane.service.CrawlerService;
import com.cochrane.library.cochrane.repository.CochraneReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final CochraneReviewRepository reviewRepository;
    private final CrawlerService crawlerService;

    @Autowired
    public ReviewController(CochraneReviewRepository reviewRepository, CrawlerService crawlerService) {
        this.reviewRepository = reviewRepository;
        this.crawlerService = crawlerService;
    }

    @GetMapping
    public ResponseEntity<Page<CochraneReview>> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String searchTerm) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<CochraneReview> reviews;

        if (topic != null && !topic.isEmpty()) {
            reviews = reviewRepository.findByTopic(topic, pageRequest);
        } else if (searchTerm != null && !searchTerm.isEmpty()) {
            reviews = reviewRepository.findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCase(
                    searchTerm, searchTerm, pageRequest);
        } else {
            reviews = reviewRepository.findAll(pageRequest);
        }

        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CochraneReview> getReview(@PathVariable Long id) {
        return reviewRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/topics")
    public ResponseEntity<List<String>> getAllTopics() {
        List<String> topics = reviewRepository.findAllTopics();
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get total reviews
        long totalReviews = reviewRepository.count();
        stats.put("totalReviews", totalReviews);

        // Get counts by status
        long failedContent = reviewRepository.countByCrawlStatus(CochraneReview.CrawlStatus.FAILED);
        long pendingContent = reviewRepository.countByCrawlStatus(CochraneReview.CrawlStatus.PENDING);
        stats.put("failedContent", failedContent);
        stats.put("pendingContent", pendingContent);

        // Get reviews by topic
        Map<String, Long> reviewsByTopic = reviewRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        CochraneReview::getTopic,
                        Collectors.counting()));
        stats.put("reviewsByTopic", reviewsByTopic);

        return stats;
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<Map<String, Object>> retryFailedContent() {
        List<CochraneReview> failedReviews = reviewRepository.findByCrawlStatus(CochraneReview.CrawlStatus.FAILED);
        int retriedCount = crawlerService.retryFailedContent(failedReviews);

        Map<String, Object> response = new HashMap<>();
        response.put("retriedCount", retriedCount);
        response.put("totalFailed", failedReviews.size());

        return ResponseEntity.ok(response);
    }
}