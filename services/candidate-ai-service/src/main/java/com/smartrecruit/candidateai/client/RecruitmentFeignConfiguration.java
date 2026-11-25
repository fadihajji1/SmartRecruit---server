package com.smartrecruit.candidateai.client;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;

import feign.Retryer;

/**
 * Client-specific Feign configuration (deliberately not a {@code @Configuration} class, so it applies only to
 * {@link RecruitmentFeignClient}).
 * <p>
 * Feign retries only {@code RetryableException}s, which are network failures (connection refused, read timeout)
 * or a 503 with Retry-After. The client only issues idempotent GET requests, so one bounded retry is safe.
 */
public class RecruitmentFeignConfiguration {

    @Bean
    Retryer recruitmentRetryer() {
        // first retry after 200 ms, never wait more than 1 s, at most 2 attempts in total
        return new Retryer.Default(200, TimeUnit.SECONDS.toMillis(1), 2);
    }
}
