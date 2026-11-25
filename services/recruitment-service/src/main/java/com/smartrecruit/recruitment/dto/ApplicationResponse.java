package com.smartrecruit.recruitment.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Application summary; CV files remain owned by Candidate/AI Service")
public record ApplicationResponse(
		UUID id,
		UUID jobId,
		UUID candidateId,
		UUID candidateCvId,
		String message,
		String jobTitle,
		String businessName,
		String candidateName,
		String candidateEmail,
		@Schema(description = "PENDING, REVIEWING, INTERVIEW, HIRED, REJECTED or UNAVAILABLE") String status,
		Instant createdAt,
		Instant updatedAt) {
}
