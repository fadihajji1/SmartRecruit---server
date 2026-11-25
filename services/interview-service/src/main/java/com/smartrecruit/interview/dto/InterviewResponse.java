package com.smartrecruit.interview.dto;

import java.time.Instant;
import java.util.UUID;

import com.smartrecruit.interview.model.DelayRequest;
import com.smartrecruit.interview.model.Interview;

public record InterviewResponse(UUID id, UUID applicationId, UUID candidateId, UUID recruiterId,
        UUID jobId, String jobTitle, String candidateName, Instant scheduledAt, int durationMinutes,
        String externalMeetingUrl, String notes, String status, DelayRequestResponse pendingDelayRequest,
        Instant createdAt, Instant updatedAt) {

    public static InterviewResponse from(Interview interview) {
        return from(interview, null);
    }

    public static InterviewResponse from(Interview interview, DelayRequest pendingDelay) {
        return new InterviewResponse(interview.getId(), interview.getApplicationId(), interview.getCandidateId(),
                interview.getRecruiterId(), interview.getJobId(), interview.getJobTitle(), interview.getCandidateName(),
                interview.getScheduledAt(), interview.getDurationMinutes(), interview.getExternalMeetingUrl(),
                interview.getNotes(), interview.getStatus().name(),
                pendingDelay == null ? null : DelayRequestResponse.from(pendingDelay, interview.getId()),
                interview.getCreatedAt(), interview.getUpdatedAt());
    }
}
