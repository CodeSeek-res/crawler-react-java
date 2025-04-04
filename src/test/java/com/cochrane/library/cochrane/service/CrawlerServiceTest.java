package com.cochrane.library.cochrane.service;

import com.cochrane.library.cochrane.model.CrawlerStatistics;
import com.cochrane.library.cochrane.model.Review;
import com.cochrane.library.cochrane.repository.CrawlerStatisticsRepository;
import com.cochrane.library.cochrane.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrawlerServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CrawlerStatisticsRepository statisticsRepository;

    private CrawlerService crawlerService;

    @BeforeEach
    void setUp() {
        crawlerService = new CrawlerService(reviewRepository, statisticsRepository);
    }

    @Test
    void startCrawler_WhenNotRunning_ShouldStartAndReturnTrue() {
        // Given
        when(statisticsRepository.save(any(CrawlerStatistics.class))).thenReturn(new CrawlerStatistics());

        // When
        boolean result = crawlerService.startCrawler();

        // Then
        assertTrue(result);
        assertTrue(crawlerService.getStatus().isRunning());
        verify(statisticsRepository).save(any(CrawlerStatistics.class));
    }

    @Test
    void startCrawler_WhenAlreadyRunning_ShouldReturnFalse() {
        // Given
        crawlerService.startCrawler();

        // When
        boolean result = crawlerService.startCrawler();

        // Then
        assertFalse(result);
    }

    @Test
    void stopCrawler_WhenRunning_ShouldStopAndReturnTrue() {
        // Given
        crawlerService.startCrawler();

        // When
        boolean result = crawlerService.stopCrawler();

        // Then
        assertTrue(result);
        assertFalse(crawlerService.getStatus().isRunning());
    }

    @Test
    void stopCrawler_WhenNotRunning_ShouldReturnFalse() {
        // When
        boolean result = crawlerService.stopCrawler();

        // Then
        assertFalse(result);
        assertFalse(crawlerService.getStatus().isRunning());
    }
}