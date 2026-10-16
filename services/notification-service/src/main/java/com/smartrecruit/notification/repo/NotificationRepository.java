package com.smartrecruit.notification.repo;
import com.smartrecruit.notification.domain.Notification;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationRepository extends JpaRepository<Notification,UUID>{
Page<Notification> findByRecipientUserIdAndDeletedAtIsNull(UUID id,Pageable p);
Optional<Notification> findByIdAndRecipientUserIdAndDeletedAtIsNull(UUID id,UUID owner);
boolean existsBySourceEventIdAndRecipientUserId(UUID sourceEventId,UUID recipientUserId);
}

