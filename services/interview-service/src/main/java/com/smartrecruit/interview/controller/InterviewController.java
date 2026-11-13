package com.smartrecruit.interview.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.smartrecruit.interview.dto.CreateInterviewRequest;
import com.smartrecruit.interview.dto.DelayRequestDto;
import com.smartrecruit.interview.dto.DelayRequestResponse;
import com.smartrecruit.interview.dto.InterviewResponse;
import com.smartrecruit.interview.dto.UpdateInterviewRequest;
import com.smartrecruit.interview.service.InterviewService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Validated
@SecurityRequirement(name = "bearerAuth")
public class InterviewController {
    private final InterviewService service;

    public InterviewController(InterviewService service) {
        this.service = service;
    }

    @GetMapping("/recruiter/interviews")
    public List<InterviewResponse> recruiterInterviews(@AuthenticationPrincipal Jwt jwt) {
        return service.getRecruiterInterviews(requireRoleAndId(jwt, "RECRUITER"));
    }

    @PostMapping("/recruiter/interviews")
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewResponse create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateInterviewRequest request) {
        return service.create(requireRoleAndId(jwt, "RECRUITER"), request);
    }

    @GetMapping("/recruiter/interviews/{interviewId}")
    public InterviewResponse recruiterInterview(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID interviewId) {
        return service.getForRecruiter(requireRoleAndId(jwt, "RECRUITER"), interviewId);
    }

    @PatchMapping("/recruiter/interviews/{interviewId}")
    public InterviewResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID interviewId,
            @Valid @RequestBody UpdateInterviewRequest request) {
        return service.update(requireRoleAndId(jwt, "RECRUITER"), interviewId, request);
    }

    @PostMapping("/recruiter/interviews/{interviewId}/cancel")
    public InterviewResponse cancelRecruiter(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID interviewId) {
        return service.cancelAsRecruiter(requireRoleAndId(jwt, "RECRUITER"), interviewId);
    }

    @PostMapping("/recruiter/interviews/{interviewId}/done")
    public InterviewResponse done(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID interviewId) {
        return service.markDone(requireRoleAndId(jwt, "RECRUITER"), interviewId);
    }

    @PostMapping("/recruiter/interviews/{interviewId}/delay-request/approve")
    public DelayRequestResponse approveDelay(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID interviewId) {
        return service.resolveDelay(requireRoleAndId(jwt, "RECRUITER"), interviewId, true);
    }

    @PostMapping("/recruiter/interviews/{interviewId}/delay-request/reject")
    public DelayRequestResponse rejectDelay(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID interviewId) {
        return service.resolveDelay(requireRoleAndId(jwt, "RECRUITER"), interviewId, false);
    }

    @GetMapping("/candidate/interviews")
    public List<InterviewResponse> candidateInterviews(@AuthenticationPrincipal Jwt jwt) {
        return service.getCandidateInterviews(requireRoleAndId(jwt, "CANDIDATE"));
    }

    @GetMapping("/candidate/interviews/{interviewId}")
    public InterviewResponse candidateInterview(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID interviewId) {
        return service.getForCandidate(requireRoleAndId(jwt, "CANDIDATE"), interviewId);
    }

    @PostMapping("/candidate/interviews/{interviewId}/cancel")
    public InterviewResponse cancelCandidate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID interviewId) {
        return service.cancelAsCandidate(requireRoleAndId(jwt, "CANDIDATE"), interviewId);
    }

    @PostMapping("/candidate/interviews/{interviewId}/delay-request")
    public DelayRequestResponse requestDelay(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID interviewId,
            @Valid @RequestBody DelayRequestDto request) {
        return service.requestDelay(requireRoleAndId(jwt, "CANDIDATE"), interviewId, request);
    }

    private static UUID requireRoleAndId(Jwt jwt, String expectedRole) {
        if (jwt == null || jwt.getClaimAsStringList("roles") == null
                || !jwt.getClaimAsStringList("roles").contains(expectedRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Required role is missing");
        }
        String userId = jwt.getClaimAsString("user_id");
        if (userId == null) {
            throw new IllegalArgumentException("JWT user_id claim is required");
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("JWT user_id claim must be a UUID");
        }
    }
}
