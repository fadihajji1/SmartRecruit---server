package com.smartrecruit.recruitment.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartrecruit.recruitment.dto.ApplicationResponse;
import com.smartrecruit.recruitment.dto.UpdateApplicationStatusRequest;
import com.smartrecruit.recruitment.service.RecruitmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recruiter/applications")
@PreAuthorize("hasRole('RECRUITER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recruiter applications")
public class RecruiterApplicationController {
	private final RecruitmentService recruitmentService;

	public RecruiterApplicationController(RecruitmentService recruitmentService) {
		this.recruitmentService = recruitmentService;
	}

	@GetMapping
	@Operation(summary = "List applications for the recruiter's businesses")
	public Page<ApplicationResponse> getApplications(
			@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
		return recruitmentService.getRecruiterApplications(userId(jwt), pageable);
	}

	@GetMapping("/{applicationId}")
	@Operation(summary = "Get an application for the recruiter's businesses")
	public ResponseEntity<ApplicationResponse> getApplication(
			@PathVariable UUID applicationId, @AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(recruitmentService.getRecruiterApplication(applicationId, userId(jwt)));
	}

	@PatchMapping("/{applicationId}/status")
	@Operation(summary = "Update application status", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UpdateApplicationStatusRequest.class),
					examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
							name = "Update application status example",
							value = """
								{
								  "status": "HIRED"
								}
								"""))))
	public ResponseEntity<ApplicationResponse> updateStatus(
			@PathVariable UUID applicationId,
			@RequestBody UpdateApplicationStatusRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(
				recruitmentService.updateApplicationStatus(applicationId, userId(jwt), request));
	}

	private UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getClaimAsString("user_id"));
	}
}
