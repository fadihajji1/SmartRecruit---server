package com.smartrecruit.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fields to update; omitted or null fields are unchanged")
public record UpdateJobRequest(
		@Schema(description = "Updated job title") String title,
		@Schema(description = "Updated offer description") String description,
		@Schema(description = "Updated work location") String location,
		@Schema(description = "Updated contract type") String contractType) {
}
