package com.smartrecruit.candidateai.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Declarative Feign client for the Recruitment Service, used to confirm that an application belongs to the
 * recruiter before their CV is shown. Timeouts live under {@code spring.cloud.openfeign.client.config.recruitment-service}.
 */
@FeignClient(name = "recruitment-service", url = "${candidate-ai.recruitment.base-url}",
        configuration = RecruitmentFeignConfiguration.class)
public interface RecruitmentFeignClient {

    /** The recruiter's own token is forwarded, so Recruitment only answers for that recruiter's applications. */
    @GetMapping("/api/v1/recruiter/applications/{applicationId}")
    ApplicationView recruiterApplication(@PathVariable("applicationId") UUID applicationId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ApplicationView(UUID id, UUID candidateId, UUID candidateCvId, String candidateName, String jobTitle,
            String status) {
    }
}
