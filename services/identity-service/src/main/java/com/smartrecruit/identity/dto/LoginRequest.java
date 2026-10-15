package com.smartrecruit.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@Email @NotBlank @Size(max = 320) String email,
		@NotBlank String password) {
}
