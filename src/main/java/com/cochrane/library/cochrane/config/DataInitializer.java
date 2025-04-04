package com.cochrane.library.cochrane.config;

import com.cochrane.library.cochrane.model.CochraneReview;
import com.cochrane.library.cochrane.repository.CochraneReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CochraneReviewRepository reviewRepository;

    @Override
    public void run(String... args) {
        if (reviewRepository.count() == 0) {
            initializeSampleData();
        }
    }

    private void initializeSampleData() {
        // COVID-19 Review
        CochraneReview covidReview = new CochraneReview();
        covidReview.setTitle("Interventions for preventing COVID-19 in healthcare settings");
        covidReview.setAuthors("Smith J, Johnson M, Williams R");
        covidReview.setTopic("Infectious Diseases");
        covidReview.setUrl("https://www.cochranelibrary.com/cdsr/doi/10.1002/example1");
        covidReview.setPublicationDate(LocalDate.of(2023, 3, 15));
        covidReview.setContent("Sample content for COVID-19 review...");
        covidReview.setCrawlStatus(CochraneReview.CrawlStatus.COMPLETED);
        reviewRepository.save(covidReview);

        // Mental Health Review
        CochraneReview mentalHealthReview = new CochraneReview();
        mentalHealthReview.setTitle("Cognitive behavioral therapy for anxiety disorders");
        mentalHealthReview.setAuthors("Brown A, Davis B, Miller C");
        mentalHealthReview.setTopic("Mental Health");
        mentalHealthReview.setUrl("https://www.cochranelibrary.com/cdsr/doi/10.1002/example2");
        mentalHealthReview.setPublicationDate(LocalDate.of(2023, 4, 1));
        mentalHealthReview.setContent("Sample content for mental health review...");
        mentalHealthReview.setCrawlStatus(CochraneReview.CrawlStatus.COMPLETED);
        reviewRepository.save(mentalHealthReview);

        // Cancer Review
        CochraneReview cancerReview = new CochraneReview();
        cancerReview.setTitle("Immunotherapy for lung cancer treatment");
        cancerReview.setAuthors("Wilson P, Taylor S");
        cancerReview.setTopic("Cancer");
        cancerReview.setUrl("https://www.cochranelibrary.com/cdsr/doi/10.1002/example3");
        cancerReview.setPublicationDate(LocalDate.of(2023, 2, 28));
        cancerReview.setContent("Sample content for cancer review...");
        cancerReview.setCrawlStatus(CochraneReview.CrawlStatus.COMPLETED);
        reviewRepository.save(cancerReview);

        // Cardiovascular Review
        CochraneReview cardioReview = new CochraneReview();
        cardioReview.setTitle("Exercise interventions for heart disease prevention");
        cardioReview.setAuthors("Anderson K, Lee H");
        cardioReview.setTopic("Cardiovascular");
        cardioReview.setUrl("https://www.cochranelibrary.com/cdsr/doi/10.1002/example4");
        cardioReview.setPublicationDate(LocalDate.of(2023, 1, 15));
        cardioReview.setContent("Sample content for cardiovascular review...");
        cardioReview.setCrawlStatus(CochraneReview.CrawlStatus.COMPLETED);
        reviewRepository.save(cardioReview);

        // Neurology Review
        CochraneReview neuroReview = new CochraneReview();
        neuroReview.setTitle("Treatment options for chronic migraines");
        neuroReview.setAuthors("Martin R, Thompson E");
        neuroReview.setTopic("Neurology");
        neuroReview.setUrl("https://www.cochranelibrary.com/cdsr/doi/10.1002/example5");
        neuroReview.setPublicationDate(LocalDate.of(2023, 3, 30));
        neuroReview.setContent("Sample content for neurology review...");
        neuroReview.setCrawlStatus(CochraneReview.CrawlStatus.COMPLETED);
        reviewRepository.save(neuroReview);
    }
}