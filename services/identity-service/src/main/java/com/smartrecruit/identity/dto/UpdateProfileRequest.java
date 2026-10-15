package com.smartrecruit.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
		@NotBlank @Size(max = 100) String fullName,
		@Size(max = 32) String phone,
		@Size(max = 255) String address,
		@Size(max = 100) String city,
		@Size(max = 100) String state,
		@Size(max = 100) String country,
		@Size(max = 32) String postalCode) {
}
