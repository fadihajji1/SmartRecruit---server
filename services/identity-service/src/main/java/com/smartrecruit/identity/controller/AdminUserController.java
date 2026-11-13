package com.smartrecruit.identity.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartrecruit.identity.dto.AdminUpdateUserRequest;
import com.smartrecruit.identity.dto.UserResponse;
import com.smartrecruit.identity.service.AdminUserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administration", description = "Platform administrator user and recruiter management")
public class AdminUserController {
	private final AdminUserService service;

	public AdminUserController(AdminUserService service) {
		this.service = service;
	}

	@GetMapping("/users")
	public List<UserResponse> users() {
		return service.list();
	}

	@GetMapping("/users/{id}")
	public UserResponse user(@PathVariable UUID id) {
		return service.get(id);
	}

	@PatchMapping("/users/{id}")
	public UserResponse update(@PathVariable UUID id, @RequestBody AdminUpdateUserRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return service.update(id, request, actingAdmin(jwt));
	}

	@PutMapping("/users/{id}")
	public UserResponse replace(@PathVariable UUID id, @RequestBody AdminUpdateUserRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return service.update(id, request, actingAdmin(jwt));
	}

	@GetMapping("/recruiters/pending")
	public List<UserResponse> pendingRecruiters() {
		return service.list().stream()
				.filter(u -> u.role().name().equals("RECRUITER") && u.status().name().equals("PENDING")).toList();
	}

	@PostMapping("/recruiters/{id}/approve")
	public UserResponse approve(@PathVariable UUID id) {
		return service.approve(id);
	}

	@PostMapping("/recruiters/{id}/reject")
	public UserResponse reject(@PathVariable UUID id) {
		return service.reject(id);
	}

	@GetMapping("/recruiters/{id}/employment-proof")
	public Map<String, String> employmentProof(@PathVariable UUID id) {
		return service.employmentProof(id);
	}

	private static UUID actingAdmin(Jwt jwt) {
		try {
			return UUID.fromString(jwt.getClaimAsString("user_id"));
		} catch (RuntimeException e) {
			return null;
		}
	}
}
