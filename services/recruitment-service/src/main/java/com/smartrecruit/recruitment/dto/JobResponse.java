package com.smartrecruit.recruitment.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Job offer returned by the Recruitment Service")
public record JobResponse(
		UUID id,
		UUID businessId,
		String title,
		String description,
		String location,
		String contractType,
		String status,
		Instant createdAt,
		Instant updatedAt) {
}
