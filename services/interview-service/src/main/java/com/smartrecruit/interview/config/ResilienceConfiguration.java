package com.smartrecruit.interview.config;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.smartrecruit.interview.client.RecruitmentClient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

/**
 * Circuit breaker for calls to the Recruitment Service.
 * After 5 calls in a window of 10, if half of them failed the breaker opens for 20 s and calls fail fast with a 503,
 * giving Recruitment time to recover. Client errors (4xx) are the caller's problem and never open the breaker.
 * The time limit covers the Feign connect/read timeouts plus one retry.
 */
@Configuration
public class ResilienceConfiguration {

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> recruitmentCircuitBreaker() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(5)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(20))
                        .permittedNumberOfCallsInHalfOpenState(2)
                        .ignoreExceptions(FeignException.FeignClientException.class, IllegalArgumentException.class)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(12))
                        .build()), RecruitmentClient.BREAKER);
    }
}
