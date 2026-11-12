package com.smartrecruit.interview.dto;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateInterviewRequest(
        @NotNull @Future Instant scheduledAt,
        @NotBlank String externalMeetingUrl) {
}
