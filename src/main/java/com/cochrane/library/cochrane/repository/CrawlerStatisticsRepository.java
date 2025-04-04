package com.cochrane.library.cochrane.repository;

import com.cochrane.library.cochrane.model.CrawlerStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlerStatisticsRepository extends JpaRepository<CrawlerStatistics, Long> {
    CrawlerStatistics findFirstByOrderByIdDesc(); // Get the latest statistics
}