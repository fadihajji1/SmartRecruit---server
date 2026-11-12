package com.smartrecruit.interview.dto;

import java.time.Instant;
import java.util.UUID;

import com.smartrecruit.interview.model.DelayRequest;

public record DelayRequestResponse(UUID id, UUID interviewId, UUID requestedByUserId,
        Instant proposedScheduledAt, String status, Instant createdAt, Instant resolvedAt) {
    public static DelayRequestResponse from(DelayRequest request, UUID interviewId) {
        return new DelayRequestResponse(request.getId(), interviewId, request.getRequestedByUserId(),
                request.getProposedScheduledAt(), request.getStatus().name(), request.getCreatedAt(),
                request.getResolvedAt());
    }
}
