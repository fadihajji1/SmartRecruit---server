package com.smartrecruit.identity.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartrecruit.identity.dto.AdminCreateUserRequest;
import com.smartrecruit.identity.dto.AdminUpdateUserRequest;
import com.smartrecruit.identity.dto.UserResponse;
import com.smartrecruit.identity.service.AdminUserService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/users")
@Validated
@PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administrator Users", description = "Platform administrator user management")
public class AdminUserController {
	private final AdminUserService adminUserService;

	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a candidate or recruiter account")
	public UserResponse create(
			@Valid @RequestPart("user") AdminCreateUserRequest request,
			@Parameter(description = "Required when role is RECRUITER", content = @Content(schema = @Schema(type = "string", format = "binary")))
			@RequestPart(value = "employmentProof", required = false) MultipartFile employmentProof) {
		return adminUserService.create(request, employmentProof);
	}

	@GetMapping
	@Operation(summary = "Get all registered users")
	public List<UserResponse> getAll() {
		return adminUserService.getAll();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a registered user")
	public UserResponse get(@PathVariable UUID id) {
		return adminUserService.get(id);
	}

	@GetMapping("/recruiters/pending")
	@Operation(summary = "Get recruiters awaiting approval")
	public List<UserResponse> getPendingRecruiters() {
		return adminUserService.getPendingRecruiters();
	}

	@PostMapping("/{id}/approve")
	@Operation(summary = "Approve a pending recruiter")
	public UserResponse approveRecruiter(@PathVariable UUID id) {
		return adminUserService.approveRecruiter(id);
	}

	@PostMapping("/{id}/reject")
	@Operation(summary = "Reject a pending recruiter")
	public UserResponse rejectRecruiter(@PathVariable UUID id) {
		return adminUserService.rejectRecruiter(id);
	}

	@GetMapping("/{id}/employment-proof")
	@Operation(summary = "Download a recruiter's employment proof")
	public ResponseEntity<byte[]> getEmploymentProof(@PathVariable UUID id) {
		var proof = adminUserService.getEmploymentProof(id);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(proof.contentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + proof.fileName() + "\"")
				.body(proof.content());
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update a registered user's profile or account status")
	public UserResponse update(@PathVariable UUID id, @Valid @RequestBody AdminUpdateUserRequest request) {
		return adminUserService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a registered user")
	public void delete(@PathVariable UUID id) {
		adminUserService.delete(id);
	}
}