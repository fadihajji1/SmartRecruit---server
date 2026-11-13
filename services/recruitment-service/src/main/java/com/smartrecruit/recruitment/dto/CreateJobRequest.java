package com.smartrecruit.recruitment.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fields required to create a job offer")
public record CreateJobRequest(
		@Schema(description = "Business that owns the offer", requiredMode = Schema.RequiredMode.REQUIRED) UUID businessId,
		@Schema(description = "Job title", example = "Java Backend Developer", requiredMode = Schema.RequiredMode.REQUIRED) String title,
		@Schema(description = "Offer description", requiredMode = Schema.RequiredMode.REQUIRED) String description,
		@Schema(description = "Work location", example = "Tunis, Tunisia") String location,
		@Schema(description = "Contract type", example = "FULL_TIME") String contractType) {
}
