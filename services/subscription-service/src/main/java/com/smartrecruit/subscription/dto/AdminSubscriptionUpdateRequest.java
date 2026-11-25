package com.smartrecruit.subscription.dto;

import java.time.Instant;
import java.util.UUID;
import jakarta.validation.constraints.Size;
import com.smartrecruit.subscription.domain.SubscriptionStatus;

public record AdminSubscriptionUpdateRequest(SubscriptionStatus status, @Size(max = 40) String planCode, Instant endsAt) {}
