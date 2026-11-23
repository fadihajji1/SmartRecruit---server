package com.smartrecruit.identity.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartrecruit.identity.dto.UserResponse;
import com.smartrecruit.identity.service.AdminUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Recruiter approval endpoints as documented in endpoints.md (/api/v1/admin/recruiters/...). */
@RestController
@RequestMapping("/api/v1/admin/recruiters")
@PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administrator Recruiters", description = "Recruiter approval workflow")
public class AdminRecruiterController {
	private final AdminUserService adminUserService;

	public AdminRecruiterController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@GetMapping("/pending")
	@Operation(summary = "Recruiters awaiting approval")
	public List<UserResponse> pending() {
		return adminUserService.getPendingRecruiters();
	}

	@PostMapping("/{id}/approve")
	@Operation(summary = "Approve a pending recruiter")
	public UserResponse approve(@PathVariable UUID id) {
		return adminUserService.approveRecruiter(id);
	}

	@PostMapping("/{id}/reject")
	@Operation(summary = "Reject a pending recruiter")
	public UserResponse reject(@PathVariable UUID id) {
		return adminUserService.rejectRecruiter(id);
	}

	@GetMapping("/{id}/employment-proof")
	@Operation(summary = "Download a recruiter's employment proof")
	public ResponseEntity<byte[]> employmentProof(@PathVariable UUID id) {
		var proof = adminUserService.getEmploymentProof(id);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(proof.contentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + proof.fileName() + "\"")
				.body(proof.content());
	}
}
