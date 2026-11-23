package com.smartrecruit.recruitment.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Business (company) profile")
public record BusinessResponse(
		UUID id,
		@Schema(description = "Recruiter who owns the business") UUID ownerUserId,
		String name,
		String description,
		String website,
		String industry,
		String address,
		String city,
		String country,
		String status,
		Instant createdAt,
		Instant updatedAt) {
}
