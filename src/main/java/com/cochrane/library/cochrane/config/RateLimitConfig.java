package com.cochrane.library.cochrane.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket tokenBucket() {
        long tokens = 100;
        Refill refill = Refill.intervally(tokens, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(tokens, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}