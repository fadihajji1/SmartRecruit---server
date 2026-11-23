package com.smartrecruit.recruitment.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Candidate's request to apply to an open job")
public record CreateApplicationRequest(
		@Schema(description = "Job offer identifier", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID jobId,
		@Schema(description = "CV identifier owned by Candidate/AI Service", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID candidateCvId,
		@Schema(description = "Optional message to the recruiter") @Size(max = 5000) String message) {
}
