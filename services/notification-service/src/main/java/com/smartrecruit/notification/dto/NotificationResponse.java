package com.smartrecruit.notification.dto;

import java.time.Instant;
import java.util.UUID;
import com.smartrecruit.notification.domain.Notification;

public record NotificationResponse(UUID id, UUID recipientUserId, UUID sourceEventId, String eventType,
        String type, String title, String content, Instant readAt, Instant createdAt) {
    public static NotificationResponse from(Notification item) {
        return new NotificationResponse(item.getId(), item.getRecipientUserId(), item.getSourceEventId(), item.getEventType(),
            item.getType(), item.getTitle(), item.getContent(), item.getReadAt(), item.getCreatedAt());
    }
}
