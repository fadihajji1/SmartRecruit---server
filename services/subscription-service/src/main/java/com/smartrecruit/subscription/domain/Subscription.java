package com.smartrecruit.subscription.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private SubscriptionAudience audience;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pending_plan_id")
    private SubscriptionPlan pendingPlan;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private SubscriptionStatus status;
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;
    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;
    @Column(name = "cancelled_at")
    private Instant cancelledAt;
    @Column(name = "change_effective_at")
    private Instant changeEffectiveAt;
    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version private long version;

    protected Subscription() {}
    public Subscription(UUID ownerId, SubscriptionAudience audience, SubscriptionPlan plan, SubscriptionStatus status,
            Instant startsAt, Instant endsAt, boolean autoRenew) {
        this.ownerId = ownerId; this.audience = audience; this.plan = plan; this.status = status;
        this.startsAt = startsAt; this.endsAt = endsAt; this.autoRenew = autoRenew;
    }
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public void upgrade(SubscriptionPlan plan, Instant startsAt, Instant endsAt, boolean autoRenew) {
        this.plan = plan; this.pendingPlan = null; this.changeEffectiveAt = null; this.status = SubscriptionStatus.ACTIVE;
        this.startsAt = startsAt; this.endsAt = endsAt; this.cancelledAt = null; this.autoRenew = autoRenew;
    }
    public void scheduleDowngrade(SubscriptionPlan plan) { this.pendingPlan = plan; this.changeEffectiveAt = this.endsAt; }
    public void applyPendingChange(Instant now) {
        if (pendingPlan != null && changeEffectiveAt != null && !changeEffectiveAt.isAfter(now)) {
            this.plan = pendingPlan; this.pendingPlan = null; this.startsAt = now;
            this.endsAt = now.plusSeconds((long) plan.getBillingPeriodDays() * 86400L);
            this.changeEffectiveAt = null; this.status = SubscriptionStatus.ACTIVE;
        }
    }
    public void renew(Instant now) {
        this.startsAt = now.isBefore(endsAt) ? startsAt : now;
        Instant base = now.isBefore(endsAt) ? endsAt : now;
        this.endsAt = base.plusSeconds((long) plan.getBillingPeriodDays() * 86400L);
        this.status = SubscriptionStatus.ACTIVE; this.cancelledAt = null; this.autoRenew = true;
    }
    public void cancel(Instant now) { this.status = SubscriptionStatus.CANCELLED; this.cancelledAt = now; this.autoRenew = false; this.pendingPlan = null; this.changeEffectiveAt = null; }
    public void setAdminStatus(SubscriptionStatus status) { this.status = status; if (status != SubscriptionStatus.ACTIVE) this.autoRenew = false; else this.cancelledAt = null; }
    public void setAdminDetails(SubscriptionStatus status, Instant endsAt) {
        if (status != null) setAdminStatus(status);
        if (endsAt != null) this.endsAt = endsAt;
    }
    public void changePlanNow(SubscriptionPlan plan, Instant now) { upgrade(plan, now, now.plusSeconds((long) plan.getBillingPeriodDays() * 86400L), autoRenew); }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public SubscriptionAudience getAudience() { return audience; }
    public SubscriptionPlan getPlan() { return plan; }
    public SubscriptionPlan getPendingPlan() { return pendingPlan; }
    public SubscriptionStatus getStatus() { return status; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getChangeEffectiveAt() { return changeEffectiveAt; }
    public boolean isAutoRenew() { return autoRenew; }
    public Instant getCreatedAt() { return createdAt; }
}
