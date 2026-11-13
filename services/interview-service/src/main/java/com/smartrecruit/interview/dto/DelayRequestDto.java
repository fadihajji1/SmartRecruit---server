package com.smartrecruit.interview.dto;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record DelayRequestDto(@NotNull @Future Instant proposedScheduledAt) {
}
