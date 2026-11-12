package com.smartrecruit.interview.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record CreateInterviewRequest(
        @NotNull UUID applicationId,
        @NotNull UUID candidateId,
        @NotNull UUID jobId,
        @NotNull @Future Instant scheduledAt,
        @NotBlank String externalMeetingUrl) {
}
