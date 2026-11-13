package com.smartrecruit.recruitment.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Business visible to an authorized recruiter")
public record BusinessResponse(
		UUID id,
		String name,
		String description,
		String address,
		String status,
		Instant createdAt,
		Instant updatedAt) {
}
