package com.smartrecruit.identity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartrecruit.identity.dto.CandidateRegistrationRequest;
import com.smartrecruit.identity.dto.LoginRequest;
import com.smartrecruit.identity.dto.RecruiterRegistrationRequest;
import com.smartrecruit.identity.dto.TokenResponse;
import com.smartrecruit.identity.dto.UpdateProfileRequest;
import com.smartrecruit.identity.dto.UserResponse;
import com.smartrecruit.identity.service.IdentityService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "1- Authentication and Profile", description = "Registration, login, and current-user profile operations")
public class AuthController {
	private final IdentityService identityService;

	public AuthController(IdentityService identityService) {
		this.identityService = identityService;
	}

	// register candidate endpoint
	@PostMapping("/auth/register/candidate")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Register a candidate", 
			description = """
				Public Candidate registration.

				Access: PUBLIC
				Resulting role: CANDIDATE
				Initial account state: ACTIVE
			""", 
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = 	
				true,
				content = @Content(mediaType = "application/json", 
				schema = @Schema(implementation = CandidateRegistrationRequest.class), examples = @ExampleObject(name = "Candidate Registration Example", 
				value = """
			{
				"full name": "Fadi Hajji",
				"email": "fadi@gmail.com",						"password": "Password123!",
				"phoneNumber": "+21612345678",
				"address": "12 Main Street",
				"city": "Tunis",
				"state": "Tunis",
				"country": "Tunisia",
				"postalCode": "1000",
			}
			"""))))
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Candidate account created", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid registration details", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class))),
			@ApiResponse(responseCode = "409", description = "Email is already registered", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class)))
	})
	public UserResponse registerCandidate(@Valid @RequestBody CandidateRegistrationRequest request) {
		return identityService.registerCandidate(request);
	}

	// register recruiter endpoint
	@PostMapping(value = "/auth/register/recruiter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Register a recruiter", 
			description = """
				Public Recruiter registration.

				Access: PUBLIC
				Resulting role: RECRUITER
				Initial account state: PENDING
				Approval: Required from a platform administrator to review the employment proof and activate the account

			""", 
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = 	
				true,
				content = @Content(mediaType = "application/json", 
				schema = @Schema(implementation = RecruiterRegistrationRequest.class), examples = @ExampleObject(name = "Recruiter Registration Example", 
				value = """
			{
				"full name": "Fadi Hajji",
				"email": "fadi@gmail.com",						"password": "Password123!",
				"phoneNumber": "+21612345678",
				"address": "12 Main Street",
				"city": "Tunis",
				"state": "Tunis",
				"country": "Tunisia",
				"postalCode": "1000",
				"documentType": "employment_contract",
                "documentFile": "<binary file>"
			}
			"""))))

	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Pending recruiter account created", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid registration details or employment proof", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class))),
			@ApiResponse(responseCode = "409", description = "Email is already registered", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class))),
			@ApiResponse(responseCode = "502", description = "Employment proof storage is unavailable", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class)))
	})
	public UserResponse registerRecruiter(
			@Parameter(
				description = "Recruiter registration details as JSON", 
				required = 
					true, 
					content = @Content(
						mediaType = MediaType.APPLICATION_JSON_VALUE, 
						schema = @Schema(
							implementation = RecruiterRegistrationRequest.class))) @Valid @RequestPart("registration") RecruiterRegistrationRequest request,
			@Parameter(
				description = "Employment proof file (PDF, JPEG, or PNG; up to 10 MB)", 
				required = 
					true, 
					content = @Content(
						mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
						schema = @Schema(
							type = "string", 
							format = "binary"))) 
							@RequestPart("employmentProof") MultipartFile employmentProof) {
		return identityService.registerRecruiter(request, employmentProof);
	}

	// login endpoint
	@PostMapping("/auth/login")
	@Operation(
		summary = "Log in", 
		description = "Authenticates an active account and returns a signed JWT access token.",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = 	
			true,
			content = @Content(mediaType = "application/json", 
			schema = @Schema(
				implementation = LoginRequest.class), 
				examples = @ExampleObject(
					name = "Login Example", 
					value = """
					{
					"email": "recruiter@example.com",
					"password": "Password123!"
					}
					"""))))

	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Invalid credentials or account is not active", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class)))
	})
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return identityService.login(request);
	}

	// get profile endpoints
	@GetMapping("/users/me")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
		summary = "Get current user profile",
		description = "Returns the profile belonging to the authenticated account.",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = 	
			true,
			content = @Content(mediaType = "application/json", 
			schema = @Schema(
				implementation = RecruiterRegistrationRequest.class),
				examples = @ExampleObject(
					name = "Recruiter profile Example", 
					value = """
					{
						"full name": "Fadi Hajji",
						"email": "fadi.hajji@example.com",
						"password": "Password123!",
						"phoneNumber": "+21612345678",
						"address": "12 Main Street",
						"city": "Tunis",
						"state": "Tunis",
						"country": "Tunisia",
						"postalCode": "1000"
					}
					"""))))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Profile returned", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class)))
	})
	public UserResponse profile(@AuthenticationPrincipal Jwt jwt) {
		return identityService.getProfile(jwt);
	}

	// update profile endpoint
	@PatchMapping("/users/me")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
		summary = "Update current user profile", 
		description = "Updates permitted profile fields for the authenticated account.",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = 	
				true,
				content = @Content(mediaType = "application/json", 
				schema = @Schema(
					implementation = RecruiterRegistrationRequest.class),
					examples = @ExampleObject(
						name = "Recruiter profile update Example", 
						value = """
						{
							"full name": "Fadi Hajji",
							"old_password": "Password123!",
							"new_password": "NewPassword123!",
							"phoneNumber": "+21612345678",
							"address": "12 Main Street",
							"city": "Tunis",
							"state": "Tunis",
							"country": "Tunisia",
							"postalCode": "1000"
						}
						"""))))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Profile updated", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid profile details", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content(schema = @Schema(implementation = com.smartrecruit.identity.dto.ApiError.class)))
	})
	public UserResponse updateProfile(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateProfileRequest request) {
		return identityService.updateProfile(jwt, request);
	}
}
