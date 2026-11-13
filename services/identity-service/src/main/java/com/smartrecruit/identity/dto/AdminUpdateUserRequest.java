package com.smartrecruit.identity.dto;

import com.smartrecruit.identity.model.AccountStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUpdateUserRequest(
		@NotBlank @Size(max = 100) String fullName,
		@Size(max = 32) String phone,
		@Size(max = 255) String address,
		@Size(max = 100) String city,
		@Size(max = 100) String state,
		@Size(max = 100) String country,
		@Size(max = 32) String postalCode,
		@NotNull AccountStatus status) {
}