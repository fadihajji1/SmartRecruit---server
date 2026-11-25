package com.smartrecruit.subscription.dto;

import java.math.BigDecimal;
import java.util.Set;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.smartrecruit.subscription.domain.SubscriptionAudience;

public record PlanRequest(
    @NotBlank @Size(max = 40) String planCode,
    @NotBlank @Size(max = 100) String displayName,
    @Size(max = 500) String description,
    @NotNull SubscriptionAudience audience,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency,
    @Min(1) @Max(366) int billingPeriodDays,
    @Min(0) @Max(90) int trialDays,
    Set<@NotBlank @Size(max = 160) String> features,
    Boolean active) {}
