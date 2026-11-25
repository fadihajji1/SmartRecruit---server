package com.smartrecruit.recruitment.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Job offer with the public profile of its business")
public record JobResponse(
		UUID id,
		UUID businessId,
		UUID createdByUserId,
		String title,
		String description,
		String location,
		String contractType,
		@Schema(description = "OPEN, CLOSED or DELETED") String status,
		Instant createdAt,
		Instant updatedAt,
		String businessName,
		String businessIndustry,
		String businessCity,
		String businessCountry,
		String businessWebsite,
		String businessDescription,
		@Schema(description = "Number of applications (recruiter/admin views only)") Long applicationCount) {
}
