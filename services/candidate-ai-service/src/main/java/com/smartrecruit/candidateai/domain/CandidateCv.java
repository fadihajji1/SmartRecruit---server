package com.smartrecruit.candidateai.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "cvs")
public class CandidateCv {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "candidate_id", nullable = false) private UUID candidateId;
    @Column(name = "file_name", nullable = false, length = 255) private String originalFilename;
    @Column(name = "object_key", nullable = false, unique = true, length = 180) private String objectKey;
    @Column(name = "content_type", nullable = false, length = 120) private String contentType;
    @Column(name = "size_bytes", nullable = false) private long sizeBytes;
    @Column(name = "sha256", nullable = false, length = 64) private String sha256;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private CvStatus status;
    @Column(name = "uploaded_at", nullable = false, updatable = false) private Instant uploadedAt;
    @Column(name = "deleted_at") private Instant deletedAt;
    @Version private long version;
    protected CandidateCv() {}
    public CandidateCv(UUID candidateId, String originalFilename, String objectKey, String contentType, long sizeBytes, String sha256) {
        this.candidateId = candidateId; this.originalFilename = originalFilename; this.objectKey = objectKey;
        this.contentType = contentType; this.sizeBytes = sizeBytes; this.sha256 = sha256; this.status = CvStatus.ACTIVE;
    }
    @PrePersist void onCreate() { uploadedAt = Instant.now(); }
    @PreUpdate void onUpdate() { if (status == CvStatus.DELETED && deletedAt == null) deletedAt = Instant.now(); }
    public void markDeleted() { status = CvStatus.DELETED; deletedAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getCandidateId() { return candidateId; }
    public String getOriginalFilename() { return originalFilename; }
    public String getObjectKey() { return objectKey; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getSha256() { return sha256; }
    public CvStatus getStatus() { return status; }
    public Instant getUploadedAt() { return uploadedAt; }
    public Instant getDeletedAt() { return deletedAt; }
}
