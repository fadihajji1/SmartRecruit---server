package com.smartrecruit.candidateai.client;

import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;

/**
 * Resilient access to the Recruitment Service through the "recruitment" circuit breaker.
 * 404/403 mean the application is not the recruiter's; failures and an open breaker become a controlled 503.
 */
@Component
public class RecruitmentClient {
    private static final Logger log = LoggerFactory.getLogger(RecruitmentClient.class);
    public static final String BREAKER = "recruitment";

    private final RecruitmentFeignClient client;
    private final CircuitBreaker breaker;

    public RecruitmentClient(RecruitmentFeignClient client, CircuitBreakerFactory<?, ?> breakers) {
        this.client = client;
        this.breaker = breakers.create(BREAKER);
    }

    public RecruitmentFeignClient.ApplicationView recruiterApplication(UUID applicationId, String bearerToken) {
        return breaker.run(() -> client.recruiterApplication(applicationId, "Bearer " + bearerToken),
                failure -> { throw translate(failure); });
    }

    private static ResponseStatusException translate(Throwable failure) {
        Throwable cause = failure;
        while ((cause instanceof ExecutionException || cause instanceof CompletionException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof FeignException.NotFound || cause instanceof FeignException.Forbidden) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "This application is not one of yours");
        }
        log.warn("Recruitment Service call failed: {}", cause.toString());
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Recruitment Service is unavailable, try again shortly");
    }
}
