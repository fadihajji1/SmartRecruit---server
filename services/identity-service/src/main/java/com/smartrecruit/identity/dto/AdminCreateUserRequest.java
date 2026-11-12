package com.smartrecruit.identity.dto;

import com.smartrecruit.identity.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
		@Email @NotBlank @Size(max = 320) String email,
		@NotBlank @Size(min = 12, max = 72) String password,
		@NotBlank @Size(max = 100) String fullName,
		@NotNull UserRole role,
		@Size(max = 32) String phone,
		@Size(max = 255) String address,
		@Size(max = 100) String city,
		@Size(max = 100) String state,
		@Size(max = 100) String country,
		@Size(max = 32) String postalCode) {
}