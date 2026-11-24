package com.smartrecruit.interview.dto;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateInterviewRequest(
        @NotNull @Future Instant scheduledAt,
        @Size(max = 2000) String externalMeetingUrl,
        @Min(15) @Max(480) Integer durationMinutes,
        @Size(max = 4000) String notes) {
}
