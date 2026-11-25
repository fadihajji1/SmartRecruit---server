package com.smartrecruit.interview.client;

import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import com.smartrecruit.interview.exception.DependencyUnavailableException;

import feign.FeignException;

/**
 * Resilient access to the Recruitment Service: the Feign call runs inside the "recruitment" circuit breaker.
 * <ul>
 * <li>404/403 from Recruitment means the application is not the recruiter's: a client error, never counted as a
 * failure by the breaker.</li>
 * <li>Network errors, timeouts, 5xx responses and an open breaker become a controlled 503
 * ({@link DependencyUnavailableException}) instead of a stack trace.</li>
 * </ul>
 */
@Component
public class RecruitmentClient {
    private static final Logger log = LoggerFactory.getLogger(RecruitmentClient.class);
    public static final String BREAKER = "recruitment";

    public record ApplicationSummary(UUID id, UUID jobId, UUID candidateId, String jobTitle, String candidateName,
            String status) {
    }

    private final RecruitmentFeignClient client;
    private final CircuitBreaker breaker;

    public RecruitmentClient(RecruitmentFeignClient client, CircuitBreakerFactory<?, ?> breakers) {
        this.client = client;
        this.breaker = breakers.create(BREAKER);
    }

    /** Returns the recruiter's application, or throws IllegalArgumentException when it is not theirs. */
    public ApplicationSummary recruiterApplication(UUID applicationId, String bearerToken) {
        RecruitmentFeignClient.ApplicationView view = breaker.run(
                () -> client.recruiterApplication(applicationId, "Bearer " + bearerToken),
                failure -> { throw translate(failure); });
        if (view == null || view.id() == null) {
            throw new DependencyUnavailableException("Recruitment Service returned no data");
        }
        return new ApplicationSummary(view.id(), view.jobId(), view.candidateId(), view.jobTitle(),
                view.candidateName(), view.status());
    }

    private static RuntimeException translate(Throwable failure) {
        Throwable cause = failure;
        while ((cause instanceof ExecutionException || cause instanceof CompletionException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof FeignException.NotFound || cause instanceof FeignException.Forbidden) {
            return new IllegalArgumentException("Application not found for this recruiter");
        }
        if (cause instanceof FeignException.FeignClientException clientError) {
            return new DependencyUnavailableException("Recruitment Service rejected the request (" + clientError.status() + ")");
        }
        if (cause instanceof TimeoutException) {
            log.warn("Recruitment Service did not answer in time");
        } else {
            log.warn("Recruitment Service call failed: {}", cause.toString());
        }
        return new DependencyUnavailableException("Recruitment Service is unavailable, try again shortly");
    }
}
