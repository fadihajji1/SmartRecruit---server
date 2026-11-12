package com.smartrecruit.notification.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "notifications", uniqueConstraints = @UniqueConstraint(name = "uk_notification_event_recipient", columnNames = {"source_event_id", "recipient_user_id"}))
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "recipient_user_id", nullable = false) private UUID recipientUserId;
    @Column(name = "source_event_id") private UUID sourceEventId;
    @Column(name = "event_type", nullable = false, length = 80) private String eventType;
    @Column(nullable = false, length = 40) private String type;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(name = "read_at") private Instant readAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "deleted_at") private Instant deletedAt;
    protected Notification() {}
    public Notification(UUID recipientUserId, UUID sourceEventId, String eventType, String type, String title, String content) {
        this.recipientUserId=recipientUserId; this.sourceEventId=sourceEventId; this.eventType=eventType;
        this.type=type; this.title=title; this.content=content;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public void markRead() { if (readAt == null) readAt = Instant.now(); }
    public void markUnread() { readAt = null; }
    public void markDeleted() { deletedAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public UUID getSourceEventId() { return sourceEventId; }
    public String getEventType() { return eventType; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Instant getReadAt() { return readAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getDeletedAt() { return deletedAt; }
}
