package com.smartrecruit.interview.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Declarative Feign client for the Recruitment Service. The base URL is the Kubernetes service name in production
 * (http://recruitment-service:8080) and localhost in development. Timeouts and logging are configured under
 * {@code spring.cloud.openfeign.client.config.recruitment-service}; the retry policy lives in
 * {@link RecruitmentFeignConfiguration}.
 */
@FeignClient(name = "recruitment-service", url = "${interview.recruitment.base-url}",
        configuration = RecruitmentFeignConfiguration.class)
public interface RecruitmentFeignClient {

    /** The recruiter's own token is forwarded, so Recruitment checks that the application belongs to them. */
    @GetMapping("/api/v1/recruiter/applications/{applicationId}")
    ApplicationView recruiterApplication(@PathVariable("applicationId") UUID applicationId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ApplicationView(UUID id, UUID jobId, UUID candidateId, String jobTitle, String candidateName,
            String status) {
    }
}
