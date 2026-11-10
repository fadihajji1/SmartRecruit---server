package com.smartrecruit.notification.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {

	@Id
	private UUID id;

	@Column(name = "recipient_email", nullable = false, length = 320)
	private String recipientEmail;

	@Column(name = "recipient_role", length = 32)
	private String recipientRole;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, length = 2000)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private NotificationType type;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "read_at")
	private Instant readAt;

	protected Notification() {
	}

	public Notification(String recipientEmail, String recipientRole, String title, String message,
			NotificationType type) {
		this.id = UUID.randomUUID();
		this.recipientEmail = recipientEmail.trim().toLowerCase();
		this.recipientRole = recipientRole != null ? recipientRole.trim().toUpperCase() : null;
		this.title = title.trim();
		this.message = message.trim();
		this.type = type != null ? type : NotificationType.GENERAL;
		this.read = false;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getRecipientEmail() {
		return recipientEmail;
	}

	public String getRecipientRole() {
		return recipientRole;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public NotificationType getType() {
		return type;
	}

	public boolean isRead() {
		return read;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getReadAt() {
		return readAt;
	}

	public void markAsRead() {
		this.read = true;
		this.readAt = Instant.now();
	}

	public void markAsUnread() {
		this.read = false;
		this.readAt = null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Notification that = (Notification) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
