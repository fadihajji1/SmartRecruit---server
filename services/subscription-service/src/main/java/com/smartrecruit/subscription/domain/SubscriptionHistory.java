package com.smartrecruit.subscription.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_history")
public class SubscriptionHistory {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "subscription_id", nullable = false) private UUID subscriptionId;
    @Column(name = "actor_user_id", nullable = false) private UUID actorUserId;
    @Column(nullable = false, length = 32) private String action;
    @Column(name = "from_status", length = 24) private String fromStatus;
    @Column(name = "to_status", nullable = false, length = 24) private String toStatus;
    @Column(name = "from_plan_code", length = 40) private String fromPlanCode;
    @Column(name = "to_plan_code", length = 40) private String toPlanCode;
    @Column(length = 500) private String details;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    protected SubscriptionHistory() {}
    public SubscriptionHistory(UUID subscriptionId, UUID actorUserId, String action, String fromStatus,
            String toStatus, String fromPlanCode, String toPlanCode, String details) {
        this.subscriptionId = subscriptionId; this.actorUserId = actorUserId; this.action = action;
        this.fromStatus = fromStatus; this.toStatus = toStatus; this.fromPlanCode = fromPlanCode;
        this.toPlanCode = toPlanCode; this.details = details; this.occurredAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getSubscriptionId() { return subscriptionId; }
    public UUID getActorUserId() { return actorUserId; }
    public String getAction() { return action; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getFromPlanCode() { return fromPlanCode; }
    public String getToPlanCode() { return toPlanCode; }
    public String getDetails() { return details; }
    public Instant getOccurredAt() { return occurredAt; }
}
