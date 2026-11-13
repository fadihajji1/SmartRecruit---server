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

@Entity
@Table(name = "job_offers")
public class JobOffer {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "business_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_job_offer_business"))
	private Business business;

	// Identity Service owns this user; intentionally no cross-service FK.
	@Column(name = "created_by_user_id", nullable = false)
	private UUID createdByUserId;

	@Column(nullable = false, length = 250)
	private String title;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String description;

	@Column(length = 250)
	private String location;

	@Column(name = "contract_type", length = 50)
	private String contractType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private JobStatus status = JobStatus.OPEN;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected JobOffer() {
	}

	public JobOffer(Business business, UUID createdByUserId, String title, String description,
			String location, String contractType) {
		this.business = Objects.requireNonNull(business);
		this.createdByUserId = Objects.requireNonNull(createdByUserId);
		this.title = Objects.requireNonNull(title);
		this.description = Objects.requireNonNull(description);
		this.location = location;
		this.contractType = contractType;
	}

	public void update(String title, String description, String location, String contractType) {
		if (title != null) this.title = title;
		if (description != null) this.description = description;
		if (location != null) this.location = location;
		if (contractType != null) this.contractType = contractType;
	}

	public void close() {
		if (status != JobStatus.DELETED) status = JobStatus.CLOSED;
	}

	public void reopen() {
		if (status == JobStatus.DELETED) {
			throw new IllegalStateException("A deleted job cannot be reopened");
		}
		status = JobStatus.OPEN;
	}

	public void markDeleted() {
		status = JobStatus.DELETED;
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
	public Business getBusiness() { return business; }
	public UUID getCreatedByUserId() { return createdByUserId; }
	public String getTitle() { return title; }
	public String getDescription() { return description; }
	public String getLocation() { return location; }
	public String getContractType() { return contractType; }
	public JobStatus getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
