package com.smartrecruit.interview.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "interviews")
public class Interview {
    @Id
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "recruiter_id", nullable = false)
    private UUID recruiterId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "external_meeting_url", nullable = false, columnDefinition = "TEXT")
    private String externalMeetingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private InterviewStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Interview() {
    }

    public Interview(UUID applicationId, UUID candidateId, UUID recruiterId, UUID jobId,
            Instant scheduledAt, String externalMeetingUrl) {
        this.id = UUID.randomUUID();
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.recruiterId = recruiterId;
        this.jobId = jobId;
        this.scheduledAt = scheduledAt;
        this.externalMeetingUrl = externalMeetingUrl;
        this.status = InterviewStatus.SCHEDULED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public UUID getApplicationId() { return applicationId; }
    public UUID getCandidateId() { return candidateId; }
    public UUID getRecruiterId() { return recruiterId; }
    public UUID getJobId() { return jobId; }
    public Instant getScheduledAt() { return scheduledAt; }
    public String getExternalMeetingUrl() { return externalMeetingUrl; }
    public InterviewStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(Instant scheduledAt, String externalMeetingUrl) {
        this.scheduledAt = scheduledAt;
        this.externalMeetingUrl = externalMeetingUrl;
        this.updatedAt = Instant.now();
    }

    public void cancel() { this.status = InterviewStatus.CANCELLED; this.updatedAt = Instant.now(); }
    public void markDone() { this.status = InterviewStatus.DONE; this.updatedAt = Instant.now(); }
}
