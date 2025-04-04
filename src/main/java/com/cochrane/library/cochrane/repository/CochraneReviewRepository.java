package com.cochrane.library.cochrane.repository;

import com.cochrane.library.cochrane.model.CochraneReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CochraneReviewRepository extends JpaRepository<CochraneReview, Long> {

    /**
     * Find a review by its URL
     * 
     * @param url The URL to search for
     * @return Optional containing the review if found, empty otherwise
     */
    Optional<CochraneReview> findByUrl(String url);

    /**
     * Find all reviews for a given topic (case-insensitive)
     * 
     * @param topic The topic to search for
     * @return List of reviews matching the topic
     */
    List<CochraneReview> findByTopicIgnoreCase(String topic);

    /**
     * Find all reviews containing the given title text (case-insensitive)
     * 
     * @param titleText The text to search for in titles
     * @return List of reviews with matching titles
     */
    List<CochraneReview> findByTitleContainingIgnoreCase(String titleText);

    /**
     * Find all reviews by a specific author (case-insensitive partial match)
     * 
     * @param authorName The author name to search for
     * @return List of reviews with matching authors
     */
    List<CochraneReview> findByAuthorsContainingIgnoreCase(String authorName);

    boolean existsByUrl(String url);

    Page<CochraneReview> findByTopic(String topic, Pageable pageable);

    Page<CochraneReview> findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCase(
            String title, String authors, Pageable pageable);

    @Query("SELECT DISTINCT r.topic FROM CochraneReview r ORDER BY r.topic")
    List<String> findAllTopics();

    long countByCrawlStatus(CochraneReview.CrawlStatus status);

    List<CochraneReview> findByCrawlStatus(CochraneReview.CrawlStatus status);
}