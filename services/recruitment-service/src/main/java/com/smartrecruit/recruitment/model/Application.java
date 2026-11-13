package com.smartrecruit.recruitment.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "applications", uniqueConstraints = @UniqueConstraint(
		name = "uk_application_job_candidate", columnNames = { "job_id", "candidate_id" }))
public class Application {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_application_job"))
	private JobOffer job;

	// Identity and Candidate/AI own these IDs; intentionally no cross-service FKs.
	@Column(name = "candidate_id", nullable = false)
	private UUID candidateId;

	@Column(name = "candidate_cv_id", nullable = false)
	private UUID candidateCvId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ApplicationStatus status = ApplicationStatus.PENDING;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Application() {
	}

	public Application(JobOffer job, UUID candidateId, UUID candidateCvId) {
		this.job = Objects.requireNonNull(job);
		this.candidateId = Objects.requireNonNull(candidateId);
		this.candidateCvId = Objects.requireNonNull(candidateCvId);
	}

	public void changeStatus(ApplicationStatus status) {
		this.status = Objects.requireNonNull(status);
	}

	public void markUnavailable() {
		status = ApplicationStatus.UNAVAILABLE;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public UUID getId() { return id; }
	public JobOffer getJob() { return job; }
	public UUID getCandidateId() { return candidateId; }
	public UUID getCandidateCvId() { return candidateCvId; }
	public ApplicationStatus getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
