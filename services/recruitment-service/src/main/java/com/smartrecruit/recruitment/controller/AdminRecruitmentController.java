package com.smartrecruit.recruitment.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smartrecruit.recruitment.dto.AdminBusinessRequest;
import com.smartrecruit.recruitment.dto.BusinessResponse;
import com.smartrecruit.recruitment.dto.JobResponse;
import com.smartrecruit.recruitment.service.RecruitmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administrator recruitment", description = "Businesses, job moderation and platform counters")
public class AdminRecruitmentController {
	private final RecruitmentService recruitmentService;

	public AdminRecruitmentController(RecruitmentService recruitmentService) {
		this.recruitmentService = recruitmentService;
	}

	@GetMapping("/businesses")
	@Operation(summary = "List every business")
	public List<BusinessResponse> businesses() {
		return recruitmentService.getAllBusinesses();
	}

	@PostMapping("/businesses")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a business for a recruiter (ownerUserId and name required)")
	public BusinessResponse createBusiness(@Valid @RequestBody AdminBusinessRequest request) {
		return recruitmentService.adminCreateBusiness(request);
	}

	@GetMapping("/businesses/{businessId}")
	@Operation(summary = "Get a business")
	public BusinessResponse business(@PathVariable UUID businessId) {
		return recruitmentService.getBusinessForAdmin(businessId);
	}

	@PatchMapping("/businesses/{businessId}")
	@Operation(summary = "Update a business profile or status; deactivating closes its open jobs")
	public BusinessResponse updateBusiness(@PathVariable UUID businessId,
			@Valid @RequestBody AdminBusinessRequest request) {
		return recruitmentService.adminUpdateBusiness(businessId, request);
	}

	@DeleteMapping("/businesses/{businessId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Deactivate a business and close its open jobs (history is kept)")
	public void deactivateBusiness(@PathVariable UUID businessId) {
		recruitmentService.adminDeactivateBusiness(businessId);
	}

	@GetMapping("/jobs")
	@Operation(summary = "List every job (moderation view)")
	public List<JobResponse> jobs() {
		return recruitmentService.getAllJobs();
	}

	@PostMapping("/jobs/{jobId}/close")
	@Operation(summary = "Close an open job that violates platform rules")
	public JobResponse closeJob(@PathVariable UUID jobId) {
		return recruitmentService.adminCloseJob(jobId);
	}

	@GetMapping("/recruitment/stats")
	@Operation(summary = "Platform counters: jobs and applications by status, businesses")
	public Map<String, Long> stats() {
		return recruitmentService.getAdminStats();
	}
}
