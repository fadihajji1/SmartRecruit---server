package com.smartrecruit.subscription.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import com.smartrecruit.subscription.domain.SubscriptionAudience;
import com.smartrecruit.subscription.domain.SubscriptionPlan;

public record PlanResponse(UUID id, String planCode, String displayName, String description,
        SubscriptionAudience audience, BigDecimal price, String currency, int billingPeriodDays,
        int trialDays, boolean active, Set<String> features, Instant createdAt, Instant updatedAt) {
    public static PlanResponse from(SubscriptionPlan plan) {
        return new PlanResponse(plan.getId(), plan.getPlanCode(), plan.getDisplayName(), plan.getDescription(),
            plan.getAudience(), plan.getPrice(), plan.getCurrency(), plan.getBillingPeriodDays(),
            plan.getTrialDays(), plan.isActive(), plan.getFeatures(), plan.getCreatedAt(), plan.getUpdatedAt());
    }
}
