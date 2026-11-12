package com.smartrecruit.notification.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartrecruit.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findAllByRecipientUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);
    Optional<Notification> findByIdAndRecipientUserIdAndDeletedAtIsNull(UUID id, UUID recipientUserId);
    boolean existsBySourceEventIdAndRecipientUserId(UUID sourceEventId, UUID recipientUserId);
}
