package com.smartrecruit.interview.client;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.smartrecruit.interview.exception.DependencyUnavailableException;

/**
 * Synchronous call to the Recruitment Service with explicit timeouts and one bounded retry on network errors.
 * The recruiter's own token is forwarded, so Recruitment enforces that the application belongs to the recruiter.
 */
@Component
public class RecruitmentClient {
    private static final Logger log = LoggerFactory.getLogger(RecruitmentClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT = new ParameterizedTypeReference<>() {};

    public record ApplicationSummary(UUID id, UUID jobId, UUID candidateId, String jobTitle, String candidateName,
            String status) {
    }

    private final RestClient http;

    public RecruitmentClient(@Value("${interview.recruitment.base-url:http://localhost:8090}") String baseUrl,
            @Value("${interview.recruitment.connect-timeout:2s}") Duration connectTimeout,
            @Value("${interview.recruitment.read-timeout:5s}") Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** Returns the recruiter's application, or throws IllegalArgumentException when it is not theirs. */
    public ApplicationSummary recruiterApplication(UUID applicationId, String bearerToken) {
        for (int attempt = 1; ; attempt++) {
            try {
                Map<String, Object> body = http.get()
                        .uri("/api/v1/recruiter/applications/{id}", applicationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .retrieve()
                        .body(JSON_OBJECT);
                if (body == null) throw new DependencyUnavailableException("Recruitment Service returned no data");
                return new ApplicationSummary(uuid(body.get("id")), uuid(body.get("jobId")),
                        uuid(body.get("candidateId")), text(body.get("jobTitle")), text(body.get("candidateName")),
                        text(body.get("status")));
            } catch (HttpClientErrorException ex) {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                    throw new IllegalArgumentException("Application not found for this recruiter");
                }
                throw new DependencyUnavailableException("Recruitment Service rejected the request: " + ex.getStatusCode());
            } catch (ResourceAccessException ex) {
                if (attempt >= 2) {
                    log.warn("Recruitment Service unreachable: {}", ex.getMessage());
                    throw new DependencyUnavailableException("Recruitment Service is unavailable, try again shortly");
                }
            } catch (RestClientResponseException ex) {
                throw new DependencyUnavailableException("Recruitment Service error: " + ex.getStatusCode());
            }
        }
    }

    private static UUID uuid(Object value) {
        return value == null ? null : UUID.fromString(value.toString());
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }
}
