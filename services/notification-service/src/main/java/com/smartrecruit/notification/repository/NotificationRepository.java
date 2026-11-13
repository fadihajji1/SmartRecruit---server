package com.smartrecruit.notification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartrecruit.notification.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	List<Notification> findByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(String recipientEmail);

	List<Notification> findByRecipientEmailIgnoreCaseAndReadOrderByCreatedAtDesc(String recipientEmail, boolean read);

	long countByRecipientEmailIgnoreCaseAndReadFalse(String recipientEmail);

	Optional<Notification> findByIdAndRecipientEmailIgnoreCase(UUID id, String recipientEmail);
}
