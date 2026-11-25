package com.smartrecruit.subscription.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "plans", uniqueConstraints = @UniqueConstraint(name = "uk_plans_code", columnNames = "plan_code"))
public class SubscriptionPlan {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank @Column(name = "plan_code", nullable = false, length = 40)
    private String planCode;
    @NotBlank @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    @Column(length = 500)
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private SubscriptionAudience audience;
    @DecimalMin("0.00") @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @NotBlank @Column(nullable = false, length = 3)
    private String currency;
    @Min(1) @Max(366) @Column(name = "billing_period_days", nullable = false)
    private int billingPeriodDays;
    @Min(0) @Max(90) @Column(name = "trial_days", nullable = false)
    private int trialDays;
    @Column(nullable = false)
    private boolean active = true;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @OrderColumn(name = "feature_order")
    @Column(name = "feature", nullable = false, length = 160)
    private List<String> features = new ArrayList<>();
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SubscriptionPlan() {}

    public SubscriptionPlan(String planCode, String displayName, String description, SubscriptionAudience audience,
            BigDecimal price, String currency, int billingPeriodDays, int trialDays, Set<String> features) {
        this.planCode = normalize(planCode);
        this.displayName = displayName.trim();
        this.description = description;
        this.audience = audience;
        this.price = price;
        this.currency = normalize(currency);
        this.billingPeriodDays = billingPeriodDays;
        this.trialDays = trialDays;
        this.features = orderedFeatures(features);
    }

    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public void update(String displayName, String description, BigDecimal price, String currency,
            int billingPeriodDays, int trialDays, Set<String> features, boolean active) {
        this.displayName = displayName.trim(); this.description = description; this.price = price;
        this.currency = normalize(currency); this.billingPeriodDays = billingPeriodDays; this.trialDays = trialDays;
        this.features = orderedFeatures(features); this.active = active;
    }

    private static List<String> orderedFeatures(Set<String> values) {
        if (values == null) return new ArrayList<>();
        return new ArrayList<>(values.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList());
    }

    private static String normalize(String value) { return value.trim().toUpperCase(java.util.Locale.ROOT); }
    public UUID getId() { return id; }
    public String getPlanCode() { return planCode; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public SubscriptionAudience getAudience() { return audience; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public int getBillingPeriodDays() { return billingPeriodDays; }
    public int getTrialDays() { return trialDays; }
    public boolean isActive() { return active; }
    public Set<String> getFeatures() { return Set.copyOf(new LinkedHashSet<>(features)); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
