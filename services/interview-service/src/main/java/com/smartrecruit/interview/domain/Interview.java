package com.smartrecruit.interview.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "interviews")
public class Interview {
    public enum Status { SCHEDULED, COMPLETED, CANCELLED }
    @Id public UUID id;
    @Column(nullable = false) public UUID applicationId;
    @Column(nullable = false) public UUID candidateId;
    @Column(nullable = false) public UUID recruiterId;
    public UUID jobId;
    public String jobTitle;
    public String candidateName;
    @Column(nullable = false) public Instant scheduledAt;
    @Column(nullable = false) public int durationMinutes = 45;
    @Column(columnDefinition = "text") public String externalMeetingUrl;
    @Column(columnDefinition = "text") public String notes;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public Status status;
    @Column(nullable = false) public Instant createdAt;
    @Column(nullable = false) public Instant updatedAt;
    @PrePersist void create() { if (id == null) id = UUID.randomUUID(); createdAt = updatedAt = Instant.now(); }
    @PreUpdate void update() { updatedAt = Instant.now(); }
}
