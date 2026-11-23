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

	@Column(length = 500)
	private String website;

	@Column(length = 120)
	private String industry;

	@Column(length = 120)
	private String city;

	@Column(length = 120)
	private String country;

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

	public Business(String name, String description, String address,
			String website, String industry, String city, String country) {
		this(name, description, address);
		this.website = website;
		this.industry = industry;
		this.city = city;
		this.country = country;
	}

	public void update(String name, String description, String address) {
		if (name != null && !name.isBlank()) this.name = name;
		if (description != null) this.description = description;
		if (address != null) this.address = address;
	}

	public void updateProfile(String website, String industry, String city, String country) {
		if (website != null) this.website = website;
		if (industry != null) this.industry = industry;
		if (city != null) this.city = city;
		if (country != null) this.country = country;
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
	public String getWebsite() { return website; }
	public String getIndustry() { return industry; }
	public String getCity() { return city; }
	public String getCountry() { return country; }
	public BusinessStatus getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
