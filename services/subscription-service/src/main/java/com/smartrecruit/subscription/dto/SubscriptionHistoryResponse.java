package com.smartrecruit.subscription.dto;

import java.time.Instant;
import java.util.UUID;
import com.smartrecruit.subscription.domain.SubscriptionHistory;

public record SubscriptionHistoryResponse(UUID id, UUID subscriptionId, String action, String fromStatus,
        String toStatus, String fromPlanCode, String toPlanCode, String details, Instant occurredAt) {
    public static SubscriptionHistoryResponse from(SubscriptionHistory item) {
        return new SubscriptionHistoryResponse(item.getId(), item.getSubscriptionId(), item.getAction(),
            item.getFromStatus(), item.getToStatus(), item.getFromPlanCode(), item.getToPlanCode(),
            item.getDetails(), item.getOccurredAt());
    }
}
