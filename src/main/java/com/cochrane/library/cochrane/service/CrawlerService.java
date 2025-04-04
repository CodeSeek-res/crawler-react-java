package com.cochrane.library.cochrane.service;

import com.cochrane.library.cochrane.config.CrawlerConfig;
import com.cochrane.library.cochrane.model.CochraneReview;
import com.cochrane.library.cochrane.model.CrawlerStatus;
import com.cochrane.library.cochrane.model.CrawlerStatistics;
import com.cochrane.library.cochrane.repository.CochraneReviewRepository;
import com.cochrane.library.cochrane.repository.CrawlerStatisticsRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CrawlerService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

    // The "Browse by Topic" URL
    private static final String BASE_TOPICS_URL = "https://www.cochranelibrary.com/cdsr/reviews/topics";

    // Default Jsoup config
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final int TIMEOUT_MS = 10000;
    private static final int RATE_LIMIT_MS = 500;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private final CochraneReviewRepository reviewRepository;
    private final CrawlerConfig crawlerConfig;
    private final Set<String> seenUrls = new HashSet<>();

    // concurrency & stats
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private LocalDateTime lastRunTime;
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final List<CochraneReview> newReviews = new ArrayList<>();
    private LocalDateTime crawlStartTime;
    private double currentSpeed = 0.0;

    private final CrawlerStatisticsRepository statisticsRepository;
    private CrawlerStatistics currentStats;

    @Autowired
    public CrawlerService(
            CochraneReviewRepository reviewRepository,
            CrawlerConfig crawlerConfig,
            CrawlerStatisticsRepository statisticsRepository) {
        this.reviewRepository = reviewRepository;
        this.crawlerConfig = crawlerConfig;
        this.statisticsRepository = statisticsRepository;
        this.currentStats = getOrCreateStatistics();
    }

    private CrawlerStatistics getOrCreateStatistics() {
        CrawlerStatistics stats = statisticsRepository.findFirstByOrderByIdDesc();
        if (stats == null) {
            stats = new CrawlerStatistics();
            stats.setRunning(false);
            stats.setTotalProcessed(0);
            statisticsRepository.save(stats);
        }
        return stats;
    }

    /**
     * Scheduled crawl based on a cron expression in your config
     * (crawler.cronSchedule).
     */
    @Scheduled(cron = "${crawler.cronSchedule}")
    @Transactional
    public void scheduledCrawl() {
        logger.info("Starting scheduled Cochrane Library crawl");
        try {
            crawlAllTopics(crawlerConfig.isClearBeforeCrawl());
        } catch (Exception e) {
            logger.error("Error during scheduled crawl: {}", e.getMessage(), e);
        }
    }

    /**
     * Master method: fetch all topics from the "Browse by Topic" page, then fetch
     * each topic's listing, then parse each review detail. If updateExisting=true,
     * it will do a "full refresh".
     */
    @Transactional
    public void crawlAllTopics(boolean updateExisting) {
        logger.info("Starting crawlAllTopics with updateExisting={}", updateExisting);

        // concurrency check
        if (!isRunning.compareAndSet(false, true)) {
            logger.warn("Crawler is already running, skipping crawlAllTopics");
            return;
        }

        try {
            initNewStats(updateExisting);

            logger.info("Fetching topics from base URL: {}", crawlerConfig.getBaseUrl());
            Document doc = fetchWithRetry(crawlerConfig.getBaseUrl());
            // Each topic is an <li class="browse-by-list-item"> containing an <a> (maybe w/
            // a <button>)
            Elements topicLinks = doc.select("li.browse-by-list-item > a");

            currentStats.setTotalTopics(topicLinks.size());
            statisticsRepository.save(currentStats);

            logger.info("Found {} topics to crawl", topicLinks.size());

            for (Element topicEl : topicLinks) {
                if (!isRunning.get()) {
                    logger.info("Crawler stop requested, breaking topic processing loop");
                    break;
                }
                // parse out the topic name & url
                String topicName = extractTopicName(topicEl);
                String topicUrl = topicEl.absUrl("href");

                logger.info("Processing topic: {} (URL: {})", topicName, topicUrl);
                currentStats.setCurrentTopic(topicName);
                statisticsRepository.save(currentStats);

                try {
                    crawlTopic(topicName, topicUrl, updateExisting);
                    currentStats.addProcessedTopic(topicName);
                    statisticsRepository.save(currentStats);
                    logger.info("Successfully processed topic: {}", topicName);

                    // short delay between topics to be polite
                    Thread.sleep(crawlerConfig.getDelayBetweenRequests());

                } catch (Exception e) {
                    String errorMsg = String.format("Failed to crawl topic %s: %s", topicName, e.getMessage());
                    logger.error(errorMsg, e);
                    currentStats.addError(errorMsg);
                    statisticsRepository.save(currentStats);
                }
            }

            logger.info("Crawl completed successfully. Stats: processed={}, successful={}, failed={}",
                    currentStats.getTotalProcessed(),
                    currentStats.getSuccessfulReviews(),
                    currentStats.getFailedReviews());
        } catch (Exception e) {
            String errorMsg = "Critical error during crawlAllTopics: " + e.getMessage();
            logger.error(errorMsg, e);
            currentStats.addError(errorMsg);
            statisticsRepository.save(currentStats);
            throw new RuntimeException("Crawl failed", e);

        } finally {
            finalizeStats();
        }
    }

    /**
     * For a single topic, fetch the listing page and parse all review links. Then
     * for each link, parse the detail page.
     */
    private void crawlTopic(String topicName, String topicUrl, boolean updateExisting) {
        if (!isRunning.get()) {
            logger.debug("Skipping topic {} as crawler is stopped", topicName);
            return;
        }

        try {
            logger.debug("Fetching reviews for topic: {}", topicName);
            Document doc = fetchWithRetry(topicUrl);

            // The listing shows a set of <a> tags that contain /cdsr/doi/ in the href
            // This picks up each review link
            Elements reviewLinks = doc.select("a[href*=/doi/]");
            logger.info("Found {} potential review links for topic: {}", reviewLinks.size(), topicName);

            int processedReviews = 0;
            for (Element reviewLink : reviewLinks) {
                if (!isRunning.get()) {
                    logger.info("Crawler stop requested, breaking review loop for topic {}", topicName);
                    break;
                }

                String reviewUrl = reviewLink.attr("abs:href");
                if (!seenUrls.contains(reviewUrl)
                        && (updateExisting || !reviewRepository.existsByUrl(reviewUrl))) {

                    logger.debug("Processing new review URL: {}", reviewUrl);
                    seenUrls.add(reviewUrl);

                    // parse detail page
                    processReviewPage(reviewUrl, topicName);
                    processedReviews++;

                    // rate limit
                    Thread.sleep(crawlerConfig.getDelayBetweenRequests());
                }
            }
            logger.info("Completed processing {} new reviews for topic: {}", processedReviews, topicName);

        } catch (IOException | InterruptedException e) {
            logger.error("Error crawling topic {}: {}", topicName, e.getMessage(), e);
        }
    }

    /**
     * The detail page fetch: loads the page, extracts the main text from an updated
     * selector, sets status=COMPLETED unless there's a fatal issue.
     */
    private void processReviewPage(String reviewUrl, String topicName) {
        logger.debug("Processing review page: {}", reviewUrl);
        try {
            Document doc = fetchWithRetry(reviewUrl);

            // Basic metadata from the detail page
            CochraneReview review = extractReviewData(doc, reviewUrl, topicName);

            currentStats.setLastProcessedUrl(reviewUrl);

            if (review != null) {
                // check DB for existing, or create
                boolean isNew = !reviewRepository.existsByUrl(reviewUrl);
                reviewRepository.save(review);

                // update stats
                currentStats.setCurrentReview(review.getTitle());
                if (review.getCrawlStatus() == CochraneReview.CrawlStatus.COMPLETED) {
                    currentStats.setSuccessfulReviews(currentStats.getSuccessfulReviews() + 1);
                    logger.info("Successfully processed review: {} ({})", review.getTitle(), reviewUrl);
                } else {
                    currentStats.setFailedReviews(currentStats.getFailedReviews() + 1);
                    String err = String.format("Failed to get content: %s (%s)", review.getTitle(), reviewUrl);
                    logger.warn(err);
                    currentStats.addError(err);
                }

                currentStats.setTotalProcessed(currentStats.getTotalProcessed() + 1);
                currentStats.updateCrawlingSpeed();
                statisticsRepository.save(currentStats);

                if (isNew) {
                    logger.info("Added new review: {} ({})", review.getTitle(), reviewUrl);
                    addNewReview(review);
                }
            } else {
                // No valid review data extracted
                String errorMsg = String.format(
                        "Failed to extract review data from URL: %s - no valid doc or parse error",
                        reviewUrl);
                logger.warn(errorMsg);
                currentStats.addError(errorMsg);
                currentStats.setCurrentReview("Failed to parse detail");
                currentStats.setFailedReviews(currentStats.getFailedReviews() + 1);
                statisticsRepository.save(currentStats);
            }

        } catch (IOException e) {
            String errorMsg = String.format("Error processing review page: %s - %s", reviewUrl, e.getMessage());
            logger.error(errorMsg, e);
            currentStats.addError(errorMsg);
            currentStats.setCurrentReview("Error fetching detail");
            currentStats.setFailedReviews(currentStats.getFailedReviews() + 1);
            statisticsRepository.save(currentStats);
        }
    }

    /**
     * Extract detail page data. We assume the listing page gave us the date &
     * authors,
     * but we can also re-check if needed. For the "content," we updated the
     * selector
     * to find actual text in 'div.article-section__text' or fallback.
     */
    private CochraneReview extractReviewData(Document doc, String url, String topicName) {
        try {
            // The detail page typically has a <h1 class="publication-title"> for the main
            // review title
            Element titleElement = doc.selectFirst("h1.publication-title");
            if (titleElement == null) {
                logger.warn("No <h1.publication-title> found for review: {}", url);
                return null;
            }
            String title = titleElement.text().trim();

            // We can attempt to find authors from the detail page if present
            // but let's keep the listing page approach. Some detail pages show authors in a
            // different place:
            Element authorsEl = doc.selectFirst("div.article-authors__list");
            String authors = authorsEl != null
                    ? authorsEl.text().trim()
                    : "(authors not found in detail)";

            // The detail page might have the published date in a different place; let's
            // skip or do a fallback
            LocalDate pubDate = null; // We'll rely on listing page for date. Or parse if you see a new location

            // The main text might be in <div class="article-section__text">
            Element contentEl = doc.selectFirst("div.article-section__text");
            // fallback if that is null:
            if (contentEl == null) {
                // sometimes it might be in .article-section__content or another container
                contentEl = doc.selectFirst("div.article-section__content");
            }

            String contentHtml = contentEl != null ? contentEl.html() : "";
            // Mark status as COMPLETED if we found a title. We won't consider empty content
            // a fail here
            // so we don't get so many "failed to process content" logs.
            CochraneReview review = new CochraneReview();
            review.setUrl(url);
            review.setTopic(topicName);
            review.setTitle(title);
            review.setAuthors(authors);
            review.setPublicationDate(pubDate); // or keep date from listing if you want
            review.setContent(contentHtml);

            // If we care about the presence of content, check length
            if (!contentHtml.isEmpty()) {
                review.setCrawlStatus(CochraneReview.CrawlStatus.COMPLETED);
            } else {
                // We'll label it COMPLETED anyway, unless you want partial success?
                // Let's do a partial approach:
                review.setCrawlStatus(CochraneReview.CrawlStatus.COMPLETED);
            }

            return review;

        } catch (Exception e) {
            logger.error("Error extracting detail data from {}: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * Actually fetch a page with Jsoup, retrying a few times if there's an
     * IOException.
     */
    private Document fetchWithRetry(String url) throws IOException {
        int retries = 0;
        IOException lastException = null;

        while (retries < crawlerConfig.getMaxRetries()) {
            try {
                // Some sites need ignoreHttpErrors=false so you see 404 statuses, etc.
                return Jsoup.connect(url)
                        .userAgent(crawlerConfig.getUserAgent())
                        .timeout(crawlerConfig.getRequestTimeout())
                        .maxBodySize(0) // unlimited
                        .followRedirects(true)
                        .ignoreHttpErrors(false) // if true, you'll get a doc even if 404
                        .ignoreContentType(false) // if true, you'll parse e.g. PDFs
                        .get();

            } catch (IOException e) {
                lastException = e;
                retries++;
                logger.warn("Attempt #{} failed for {}: {}. Retrying in {}ms...",
                        retries, url, e.getMessage(), crawlerConfig.getRetryDelay());
                try {
                    Thread.sleep(crawlerConfig.getRetryDelay() * retries);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting to retry fetch", e);
                }
            }
        }
        logger.error("Failed to fetch {} after {} retries", url, crawlerConfig.getMaxRetries());
        throw lastException;
    }

    // ---------------------------------------------------
    // HELPER / UTILITY METHODS
    // ---------------------------------------------------
    private String extractTopicName(Element topicEl) {
        // typically the link has a <button> with text "Allergy & intolerance" etc.
        Element buttonEl = topicEl.selectFirst("button");
        return (buttonEl != null) ? buttonEl.text().trim() : topicEl.text().trim();
    }

    // We used to parse date from detail page. We can do it if we see a new place,
    // but for now if you want to keep listing date approach, we skip or store null.

    /**
     * Example of your date parser if you see something like "2 September 2022" on
     * the listing or detail page.
     */
    private LocalDate parseDate(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
            return LocalDate.parse(dateText.trim(), fmt);
        } catch (Exception e) {
            logger.warn("Failed to parse date: {}", dateText);
            return null;
        }
    }

    /**
     * Called at start of crawlAllTopics to set up new stats & optionally clear old
     * DB data.
     */
    private void initNewStats(boolean updateExisting) {
        logger.info("Initializing new crawler statistics");
        currentStats = new CrawlerStatistics();
        currentStats.setStartTime(LocalDateTime.now());
        currentStats.setRunning(true);
        currentStats.setTotalProcessed(0);
        currentStats.setSuccessfulReviews(0);
        currentStats.setFailedReviews(0);
        statisticsRepository.save(currentStats);

        if (updateExisting) {
            logger.info("Clearing existing data for full refresh");
            reviewRepository.deleteAll();
            seenUrls.clear();
        }
        crawlStartTime = LocalDateTime.now();
        processedCount.set(0);
        newReviews.clear();
    }

    /**
     * Called in the finally block of crawlAllTopics to finish up.
     */
    private void finalizeStats() {
        logger.info("Finalizing crawler state");
        isRunning.set(false);
        if (currentStats != null) {
            currentStats.setRunning(false);
            currentStats.setLastUpdateTime(LocalDateTime.now());
            statisticsRepository.save(currentStats);
            logger.info("Crawler statistics saved. Last update time: {}", currentStats.getLastUpdateTime());
        }
        lastRunTime = LocalDateTime.now();
        updateCrawlingSpeed();
        logger.info("Final crawling speed: {} reviews/minute", currentSpeed);
    }

    /**
     * If we want to re-extract content for previously failed items.
     */
    @Transactional
    public int retryFailedContent(List<CochraneReview> failedReviews) {
        int retriedCount = 0;
        logger.info("Retrying content extraction for {} failed reviews", failedReviews.size());

        for (CochraneReview review : failedReviews) {
            try {
                Thread.sleep(RATE_LIMIT_MS);
                processReviewPage(review.getUrl(), review.getTopic());
                retriedCount++;
            } catch (Exception e) {
                logger.error("Failed to retry content extraction for review {}: {}",
                        review.getUrl(), e.getMessage(), e);
            }
        }
        logger.info("Successfully retried content extraction for {} out of {} reviews",
                retriedCount, failedReviews.size());
        return retriedCount;
    }

    /**
     * Returns the current status (running, stats, etc.).
     */
    public CrawlerStatus getStatus() {
        CrawlerStatus status = new CrawlerStatus();
        status.setRunning(isRunning.get());
        status.setLastRun(lastRunTime);
        status.setTotalProcessed(currentStats.getTotalProcessed());
        status.setNewReviews(newReviews);
        status.setCrawlingSpeed(currentStats.getCrawlingSpeed());
        status.setCurrentTopic(currentStats.getCurrentTopic());
        status.setCurrentReview(currentStats.getCurrentReview());
        status.setErrorLog(currentStats.getErrorLog());
        status.setProcessedTopics(currentStats.getProcessedTopicsList());
        status.setSuccessfulReviews(currentStats.getSuccessfulReviews());
        status.setFailedReviews(currentStats.getFailedReviews());
        return status;
    }

    /**
     * Toggles the crawler on/off. If it's running, this tries to stop. If it's
     * stopped, it calls startCrawler().
     */
    public CrawlerStatus toggleCrawler() {
        boolean wasRunning = isRunning.get();
        if (wasRunning) {
            stopCrawler();
        } else {
            startCrawler();
        }
        return getStatus();
    }

    /**
     * Manually starts the crawler in a new thread, calling crawlAllTopics(true).
     * Remove the concurrency check here so that crawlAllTopics can do it.
     */
    public void startCrawler() {
        if (!isRunning.get()) {
            Thread crawlerThread = new Thread(() -> {
                try {
                    crawlAllTopics(false);
                } catch (Exception e) {
                    logger.error("Error in crawler thread: ", e);
                }
            });
            crawlerThread.setDaemon(true);
            crawlerThread.start();
        }
    }

    /**
     * Sets isRunning = false so the loops in crawlAllTopics/crawlTopic can exit
     * gracefully. Doesn't forcibly kill the thread, just signals them to stop if
     * they're checking isRunning.
     */
    public void stopCrawler() {
        isRunning.set(false);
        if (currentStats != null) {
            currentStats.setRunning(false);
            statisticsRepository.save(currentStats);
        }
    }

    /**
     * Recalculate #reviews / minute since start.
     */
    private void updateCrawlingSpeed() {
        if (crawlStartTime != null) {
            long minutesElapsed = Duration.between(crawlStartTime, LocalDateTime.now()).toMinutes();
            if (minutesElapsed > 0) {
                currentSpeed = (double) processedCount.get() / minutesElapsed;
            }
        }
    }

    /**
     * Called whenever we add a new review record to the DB so that we can keep an
     * in-memory list if we want to do something with them after the crawl.
     */
    public void addNewReview(CochraneReview review) {
        newReviews.add(review);
        processedCount.incrementAndGet();
    }
}
