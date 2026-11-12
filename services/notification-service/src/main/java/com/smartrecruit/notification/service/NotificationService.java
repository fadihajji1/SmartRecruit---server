package com.smartrecruit.notification.service;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.smartrecruit.notification.domain.Notification;
import com.smartrecruit.notification.dto.NotificationResponse;
import com.smartrecruit.notification.repository.NotificationRepository;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    public NotificationService(NotificationRepository notifications) { this.notifications = notifications; }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(UUID userId, Pageable pageable) {
        return notifications.findAllByRecipientUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable).map(NotificationResponse::from);
    }

    @Transactional
    public NotificationResponse read(UUID userId, UUID notificationId) {
        Notification item = owned(userId, notificationId); item.markRead(); return NotificationResponse.from(notifications.save(item));
    }

    @Transactional
    public NotificationResponse unread(UUID userId, UUID notificationId) {
        Notification item = owned(userId, notificationId); item.markUnread(); return NotificationResponse.from(notifications.save(item));
    }

    @Transactional
    public void delete(UUID userId, UUID notificationId) { Notification item = owned(userId, notificationId); item.markDeleted(); notifications.save(item); }

    @Transactional
    public void createFromEvent(UUID eventId, String eventType, Set<UUID> recipients, String title, String content, String type) {
        if (eventId == null) return;
        for (UUID recipient : recipients) {
            if (recipient == null || notifications.existsBySourceEventIdAndRecipientUserId(eventId, recipient)) continue;
            notifications.save(new Notification(recipient, eventId, eventType, type, title, content));
        }
    }

    private Notification owned(UUID userId, UUID notificationId) {
        return notifications.findByIdAndRecipientUserIdAndDeletedAtIsNull(notificationId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification was not found"));
    }
}
