package com.smartrecruit.recruitment.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smartrecruit.recruitment.dto.ApplicationResponse;
import com.smartrecruit.recruitment.dto.CreateApplicationRequest;
import com.smartrecruit.recruitment.service.RecruitmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('CANDIDATE')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Candidate applications")
public class CandidateApplicationController {
	private final RecruitmentService recruitmentService;

	public CandidateApplicationController(RecruitmentService recruitmentService) {
		this.recruitmentService = recruitmentService;
	}

	@PostMapping("/applications")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Apply to an open job", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateApplicationRequest.class),
					examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
							name = "Application submission example",
							value = """
								{
								  "jobId": "4d4d93e7-a01c-45ab-9dae-c787bf354611",
								  "candidateCvId": "b8a7b6e5-63bd-4c65-a254-42c80bca3ff2"
								}
								"""))))
	public ApplicationResponse apply(
			@RequestBody CreateApplicationRequest request, @AuthenticationPrincipal Jwt jwt) {
		return recruitmentService.createApplication(userId(jwt), request);
	}

	@GetMapping("/candidate/applications")
	@Operation(summary = "List the candidate's applications")
	public Page<ApplicationResponse> getApplications(
			@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
		return recruitmentService.getCandidateApplications(userId(jwt), pageable);
	}

	@GetMapping("/candidate/applications/{applicationId}")
	@Operation(summary = "Get one of the candidate's applications")
	public ResponseEntity<ApplicationResponse> getApplication(
			@PathVariable UUID applicationId, @AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(recruitmentService.getCandidateApplication(applicationId, userId(jwt)));
	}

	private UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getClaimAsString("user_id"));
	}
}
