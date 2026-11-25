package com.smartrecruit.candidateai.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_projection")
public class JobProjection {
    @Id @Column(name = "job_id") private UUID jobId;
    @Column(name = "business_id") private UUID businessId;
    @Column(name = "recruiter_id") private UUID recruiterId;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;
    @Column(length = 160) private String location;
    @Column(nullable = false, length = 24) private String status;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected JobProjection() {}
    public JobProjection(UUID jobId, UUID businessId, UUID recruiterId, String title, String description, String location, String status, Instant updatedAt) {
        this.jobId = jobId; this.businessId = businessId; this.recruiterId = recruiterId; this.title = title;
        this.description = description; this.location = location; this.status = status; this.updatedAt = updatedAt;
    }
    public void update(UUID businessId, UUID recruiterId, String title, String description, String location, String status, Instant updatedAt) {
        if (businessId != null) this.businessId = businessId;
        if (recruiterId != null) this.recruiterId = recruiterId;
        if (title != null && !title.isBlank()) this.title = title;
        if (description != null) this.description = description;
        if (location != null) this.location = location;
        this.status = status; this.updatedAt = updatedAt;
    }
    public UUID getJobId() { return jobId; }
    public UUID getBusinessId() { return businessId; }
    public UUID getRecruiterId() { return recruiterId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public Instant getUpdatedAt() { return updatedAt; }
}
