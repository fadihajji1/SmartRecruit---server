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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "business_members", uniqueConstraints = @UniqueConstraint(
		name = "uk_business_member_user", columnNames = { "business_id", "user_id" }))
public class BusinessMember {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "business_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_business_member_business"))
	private Business business;

	// Identity Service owns this user; intentionally no cross-service FK.
	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private BusinessMemberRole role;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected BusinessMember() {
	}

	public BusinessMember(Business business, UUID userId, BusinessMemberRole role) {
		this.business = Objects.requireNonNull(business);
		this.userId = Objects.requireNonNull(userId);
		this.role = Objects.requireNonNull(role);
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public UUID getId() { return id; }
	public Business getBusiness() { return business; }
	public UUID getUserId() { return userId; }
	public BusinessMemberRole getRole() { return role; }
	public Instant getCreatedAt() { return createdAt; }
}
