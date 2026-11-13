package com.smartrecruit.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.smartrecruit.notification.model.Notification;
import com.smartrecruit.notification.model.NotificationType;

public record NotificationResponse(
		UUID id,
		String recipientEmail,
		String recipientRole,
		String title,
		String message,
		NotificationType type,
		boolean read,
		Instant createdAt,
		Instant readAt
) {
	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getRecipientEmail(),
				notification.getRecipientRole(),
				notification.getTitle(),
				notification.getMessage(),
				notification.getType(),
				notification.isRead(),
				notification.getCreatedAt(),
				notification.getReadAt()
		);
	}
}
