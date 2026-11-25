package com.smartrecruit.recruitment.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** A domain event waiting to be published to Kafka (transactional outbox). */
@Entity
@Table(name = "event_outbox")
public class OutboxMessage {
	@Id
	private UUID id;

	@Column(name = "event_type", nullable = false, length = 80)
	private String eventType;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(nullable = false)
	private int attempts;

	@Column(name = "last_error", columnDefinition = "TEXT")
	private String lastError;

	protected OutboxMessage() {
	}

	public OutboxMessage(UUID id, String eventType, String payload) {
		this.id = id;
		this.eventType = eventType;
		this.payload = payload;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) createdAt = Instant.now();
	}

	public void markPublished() {
		publishedAt = Instant.now();
		lastError = null;
	}

	public void markFailed(String error) {
		attempts++;
		lastError = error == null ? null : error.substring(0, Math.min(error.length(), 2000));
	}

	public UUID getId() { return id; }
	public String getEventType() { return eventType; }
	public String getPayload() { return payload; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getPublishedAt() { return publishedAt; }
	public int getAttempts() { return attempts; }
	public String getLastError() { return lastError; }
}
