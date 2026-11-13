package com.smartrecruit.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Recruiter's application status change")
public record UpdateApplicationStatusRequest(
		@Schema(description = "New status: PENDING, HIRED, or REJECTED", example = "HIRED", requiredMode = Schema.RequiredMode.REQUIRED) String status) {
}
