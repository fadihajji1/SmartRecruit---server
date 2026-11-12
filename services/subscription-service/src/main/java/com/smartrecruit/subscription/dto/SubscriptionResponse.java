package com.smartrecruit.subscription.dto;

import java.time.Instant;
import java.util.UUID;
import com.smartrecruit.subscription.domain.Subscription;
import com.smartrecruit.subscription.domain.SubscriptionAudience;
import com.smartrecruit.subscription.domain.SubscriptionStatus;

public record SubscriptionResponse(UUID id, UUID ownerId, SubscriptionAudience audience, String planCode,
        String planName, SubscriptionStatus status, Instant startsAt, Instant endsAt, Instant cancelledAt,
        boolean autoRenew, String pendingPlanCode, Instant changeEffectiveAt, Instant createdAt) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(subscription.getId(), subscription.getOwnerId(), subscription.getAudience(),
            subscription.getPlan().getPlanCode(), subscription.getPlan().getDisplayName(), subscription.getStatus(),
            subscription.getStartsAt(), subscription.getEndsAt(), subscription.getCancelledAt(), subscription.isAutoRenew(),
            subscription.getPendingPlan() == null ? null : subscription.getPendingPlan().getPlanCode(),
            subscription.getChangeEffectiveAt(), subscription.getCreatedAt());
    }
}
