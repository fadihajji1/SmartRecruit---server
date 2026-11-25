package com.smartrecruit.interview.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "delay_requests")
public class DelayRequest {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(name = "requested_by_user_id", nullable = false)
    private UUID requestedByUserId;

    @Column(name = "proposed_scheduled_at", nullable = false)
    private Instant proposedScheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private DelayRequestStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected DelayRequest() {
    }

    public DelayRequest(Interview interview, UUID requestedByUserId, Instant proposedScheduledAt) {
        this.id = UUID.randomUUID();
        this.interview = interview;
        this.requestedByUserId = requestedByUserId;
        this.proposedScheduledAt = proposedScheduledAt;
        this.status = DelayRequestStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getRequestedByUserId() { return requestedByUserId; }
    public Instant getProposedScheduledAt() { return proposedScheduledAt; }
    public DelayRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }

    public void approve() { this.status = DelayRequestStatus.APPROVED; this.resolvedAt = Instant.now(); }
    public void reject() { this.status = DelayRequestStatus.REJECTED; this.resolvedAt = Instant.now(); }
}
