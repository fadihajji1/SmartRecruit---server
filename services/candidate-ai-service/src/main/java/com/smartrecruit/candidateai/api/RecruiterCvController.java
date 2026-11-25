package com.smartrecruit.candidateai.api;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartrecruit.candidateai.client.RecruitmentClient;
import com.smartrecruit.candidateai.client.RecruitmentFeignClient;
import com.smartrecruit.candidateai.service.CandidateAiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Lets a recruiter read the CV a candidate sent with an application. Ownership is checked by the Recruitment
 * Service (the recruiter's token is forwarded); the file is streamed from private storage and never exposed as a URL.
 */
@RestController
@RequestMapping("/api/v1/recruiter")
@PreAuthorize("hasRole('RECRUITER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recruiter CV access", description = "CVs attached to the recruiter's own applications")
public class RecruiterCvController {
    private final RecruitmentClient recruitment;
    private final CandidateAiService service;

    public RecruiterCvController(RecruitmentClient recruitment, CandidateAiService service) {
        this.recruitment = recruitment;
        this.service = service;
    }

    @GetMapping("/applications/{applicationId}/cv")
    @Operation(summary = "Open the CV sent with one of my applications")
    public ResponseEntity<byte[]> applicationCv(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID applicationId) {
        RecruitmentFeignClient.ApplicationView application = recruitment.recruiterApplication(applicationId, jwt.getTokenValue());
        if (application.candidateId() == null || application.candidateCvId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No CV is attached to this application");
        }
        CandidateAiService.DownloadedCv cv;
        try {
            cv = service.download(application.candidateId(), application.candidateCvId());
        } catch (ResponseStatusException missing) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The candidate has deleted this CV");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(cv.contentType()));
        // inline, so a PDF opens in the browser's viewer; Word files are still downloaded by the browser
        headers.setContentDisposition(ContentDisposition.inline().filename(cv.filename(), StandardCharsets.UTF_8).build());
        headers.setCacheControl(CacheControl.noStore());
        headers.setContentLength(cv.contents().length);
        return ResponseEntity.ok().headers(headers).body(cv.contents());
    }
}
