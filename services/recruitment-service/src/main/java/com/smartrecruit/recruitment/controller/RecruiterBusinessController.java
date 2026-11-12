package com.smartrecruit.recruitment.controller;

import java.util.UUID;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartrecruit.recruitment.dto.BusinessResponse;
import com.smartrecruit.recruitment.dto.UpdateBusinessRequest;
import com.smartrecruit.recruitment.service.RecruitmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recruiter/businesses")
@PreAuthorize("hasRole('RECRUITER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recruiter businesses")
public class RecruiterBusinessController {
	private final RecruitmentService recruitmentService;

	public RecruiterBusinessController(RecruitmentService recruitmentService) {
		this.recruitmentService = recruitmentService;
	}

	@GetMapping
	@Operation(summary = "List businesses accessible to the recruiter")
	public List<BusinessResponse> getBusinesses(@AuthenticationPrincipal Jwt jwt) {
		return recruitmentService.getRecruiterBusinesses(userId(jwt));
	}

	@GetMapping("/{businessId}")
	@Operation(summary = "Get an authorized business")
	public ResponseEntity<BusinessResponse> getBusiness(
			@PathVariable UUID businessId, @AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(recruitmentService.getRecruiterBusiness(businessId, userId(jwt)));
	}

	@PatchMapping("/{businessId}")
	@Operation(summary = "Update an authorized business", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UpdateBusinessRequest.class),
					examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
							name = "Business update example",
							value = """
								{
								  "name": "SmartRecruit Tunisia",
								  "description": "Recruitment and talent services",
								  "address": "12 Main Street, Tunis"
								}
								"""))))
	public ResponseEntity<BusinessResponse> updateBusiness(
			@PathVariable UUID businessId,
			@RequestBody UpdateBusinessRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(recruitmentService.updateRecruiterBusiness(businessId, userId(jwt), request));
	}

	private UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getClaimAsString("user_id"));
	}
}
