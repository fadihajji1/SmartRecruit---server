package com.smartrecruit.recruitment.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartrecruit.recruitment.dto.CreateJobRequest;
import com.smartrecruit.recruitment.dto.JobResponse;
import com.smartrecruit.recruitment.dto.UpdateJobRequest;
import com.smartrecruit.recruitment.service.RecruitmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recruiter/jobs")
@PreAuthorize("hasRole('RECRUITER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recruiter jobs")
public class RecruiterJobController {
	private final RecruitmentService recruitmentService;

	public RecruiterJobController(RecruitmentService recruitmentService) {
		this.recruitmentService = recruitmentService;
	}

	@GetMapping
	@Operation(summary = "List jobs managed by the recruiter")
	public Page<JobResponse> getJobs(
			@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt, Pageable pageable) {
		return recruitmentService.getRecruiterJobs(userId(jwt), pageable);
	}

	@PostMapping
	@Operation(summary = "Create a job", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateJobRequest.class),
					examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
							name = "Create job example",
							value = """
								{
								  "businessId": "4d4d93e7-a01c-45ab-9dae-c787bf354611",
								  "title": "Java Backend Developer",
								  "description": "Build and maintain Spring Boot services.",
								  "location": "Tunis, Tunisia",
								  "contractType": "FULL_TIME"
								}
								"""))))
	public ResponseEntity<JobResponse> createJob(
			@RequestBody CreateJobRequest request, @AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(recruitmentService.createRecruiterJob(userId(jwt), request));
	}

	@GetMapping("/{jobId}")
	@Operation(summary = "Get a job managed by the recruiter")
	public ResponseEntity<JobResponse> getJob(
			@PathVariable UUID jobId, @AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(recruitmentService.getRecruiterJob(jobId, userId(jwt)));
	}

	@PatchMapping("/{jobId}")
	@Operation(summary = "Update a job", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UpdateJobRequest.class),
					examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
							name = "Update job example",
							value = """
								{
								  "title": "Senior Java Backend Developer",
								  "description": "Build scalable Spring Boot services.",
								  "location": "Tunis, Tunisia",
								  "contractType": "FULL_TIME"
								}
								"""))))
	public ResponseEntity<JobResponse> updateJob(
			@PathVariable UUID jobId,
			@RequestBody UpdateJobRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(recruitmentService.updateRecruiterJob(jobId, userId(jwt), request));
	}

	@PostMapping("/{jobId}/close")
	@Operation(summary = "Close a job to new applications")
	public ResponseEntity<Void> closeJob(
			@PathVariable UUID jobId, @AuthenticationPrincipal Jwt jwt) {
		recruitmentService.closeRecruiterJob(jobId, userId(jwt));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{jobId}/reopen")
	@Operation(summary = "Reopen a job")
	public ResponseEntity<Void> reopenJob(
			@PathVariable UUID jobId, @AuthenticationPrincipal Jwt jwt) {
		recruitmentService.reopenRecruiterJob(jobId, userId(jwt));
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{jobId}")
	@Operation(summary = "Soft-delete a job while retaining its applications")
	public ResponseEntity<Void> deleteJob(
			@PathVariable UUID jobId, @AuthenticationPrincipal Jwt jwt) {
		recruitmentService.deleteRecruiterJob(jobId, userId(jwt));
		return ResponseEntity.noContent().build();
	}

	private UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getClaimAsString("user_id"));
	}
}
