package com.smartrecruit.recruitment.model;

import java.time.Instant;
import java.util.Objects;
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

@Entity
@Table(name = "businesses")
public class Business {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(length = 2000)
	private String description;

	@Column(length = 500)
	private String address;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BusinessStatus status = BusinessStatus.ACTIVE;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Business() {
	}

	public Business(String name, String description, String address) {
		this.name = Objects.requireNonNull(name, "Business name is required");
		this.description = description;
		this.address = address;
	}

	public void update(String name, String description, String address) {
		if (name != null) this.name = name;
		if (description != null) this.description = description;
		if (address != null) this.address = address;
	}

	public void setStatus(BusinessStatus status) {
		this.status = Objects.requireNonNull(status);
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
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getAddress() { return address; }
	public BusinessStatus getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
