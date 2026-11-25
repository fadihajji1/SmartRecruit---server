package com.smartrecruit.interview.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Schedule an interview for one of the recruiter's applications. Candidate, job and names are "
        + "taken from the Recruitment Service; the request values are only used if it cannot be reached.")
public record CreateInterviewRequest(
        @NotNull UUID applicationId,
        UUID candidateId,
        UUID jobId,
        @NotNull @Future Instant scheduledAt,
        @Size(max = 2000) String externalMeetingUrl,
        @Min(15) @Max(480) Integer durationMinutes,
        @Size(max = 4000) String notes,
        @Size(max = 250) String jobTitle,
        @Size(max = 200) String candidateName) {
}
