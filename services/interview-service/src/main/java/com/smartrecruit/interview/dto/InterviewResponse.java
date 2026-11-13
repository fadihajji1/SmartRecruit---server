package com.smartrecruit.interview.dto;

import java.time.Instant;
import java.util.UUID;

import com.smartrecruit.interview.model.Interview;

public record InterviewResponse(UUID id, UUID applicationId, UUID candidateId, UUID recruiterId,
        UUID jobId, Instant scheduledAt, String externalMeetingUrl, String status,
        Instant createdAt, Instant updatedAt) {
    public static InterviewResponse from(Interview interview) {
        return new InterviewResponse(interview.getId(), interview.getApplicationId(), interview.getCandidateId(),
                interview.getRecruiterId(), interview.getJobId(), interview.getScheduledAt(),
                interview.getExternalMeetingUrl(), interview.getStatus().name(), interview.getCreatedAt(),
                interview.getUpdatedAt());
    }
}
