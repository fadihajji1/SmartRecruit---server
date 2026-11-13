package com.smartrecruit.recruitment.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Candidate's request to apply to an open job")
public record CreateApplicationRequest(
		@Schema(description = "Job offer identifier", requiredMode = Schema.RequiredMode.REQUIRED) UUID jobId,
		@Schema(description = "CV identifier owned by Candidate/AI Service", requiredMode = Schema.RequiredMode.REQUIRED) UUID candidateCvId) {
}
