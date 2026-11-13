package com.smartrecruit.notification.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartrecruit.notification.dto.CreateNotificationRequest;
import com.smartrecruit.notification.dto.NotificationResponse;
import com.smartrecruit.notification.dto.UnreadCountResponse;
import com.smartrecruit.notification.exception.ForbiddenNotificationAccessException;
import com.smartrecruit.notification.exception.NotificationNotFoundException;
import com.smartrecruit.notification.model.Notification;
import com.smartrecruit.notification.model.NotificationType;
import com.smartrecruit.notification.repository.NotificationRepository;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@Transactional(readOnly = true)
	public List<NotificationResponse> getNotificationsForUser(String userEmail, Boolean readFilter) {
		String email = normalizeEmail(userEmail);
		List<Notification> list;
		if (readFilter != null) {
			list = notificationRepository.findByRecipientEmailIgnoreCaseAndReadOrderByCreatedAtDesc(email, readFilter);
		} else {
			list = notificationRepository.findByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(email);
		}
		return list.stream().map(NotificationResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public UnreadCountResponse getUnreadCount(String userEmail) {
		String email = normalizeEmail(userEmail);
		long count = notificationRepository.countByRecipientEmailIgnoreCaseAndReadFalse(email);
		return new UnreadCountResponse(count);
	}

	@Transactional
	public NotificationResponse markAsRead(UUID notificationId, String userEmail) {
		Notification notification = findAndVerifyOwnership(notificationId, userEmail);
		notification.markAsRead();
		return NotificationResponse.from(notificationRepository.save(notification));
	}

	@Transactional
	public NotificationResponse markAsUnread(UUID notificationId, String userEmail) {
		Notification notification = findAndVerifyOwnership(notificationId, userEmail);
		notification.markAsUnread();
		return NotificationResponse.from(notificationRepository.save(notification));
	}

	@Transactional
	public void deleteNotification(UUID notificationId, String userEmail) {
		Notification notification = findAndVerifyOwnership(notificationId, userEmail);
		notificationRepository.delete(notification);
	}

	@Transactional
	public NotificationResponse createNotification(CreateNotificationRequest request) {
		Notification notification = new Notification(
				request.recipientEmail(),
				request.recipientRole(),
				request.title(),
				request.message(),
				request.type()
		);
		return NotificationResponse.from(notificationRepository.save(notification));
	}

	@Transactional
	public List<NotificationResponse> seedSampleNotifications(String userEmail, String role) {
		String email = normalizeEmail(userEmail);
		String userRole = role != null ? role.toUpperCase() : "CANDIDATE";

		List<Notification> samples;
		if ("RECRUITER".equals(userRole)) {
			samples = List.of(
					new Notification(email, "RECRUITER", "New Application Received",
							"A candidate (Alice Johnson) applied for Senior Java Developer position.",
							NotificationType.APPLICATION_UPDATE),
					new Notification(email, "RECRUITER", "Interview Scheduled",
							"Technical interview confirmed for tomorrow at 10:00 AM.",
							NotificationType.INTERVIEW_SCHEDULED),
					new Notification(email, "RECRUITER", "Profile Verified",
							"Your recruiter account and employment proof have been approved by admin.",
							NotificationType.SYSTEM_ALERT)
			);
		} else {
			samples = List.of(
					new Notification(email, "CANDIDATE", "Application Shortlisted",
							"Your application for Lead Backend Engineer has been moved to Shortlisted.",
							NotificationType.APPLICATION_UPDATE),
					new Notification(email, "CANDIDATE", "Interview Invitation",
							"You have been invited for an interview with TechCorp HR team.",
							NotificationType.INTERVIEW_SCHEDULED),
					new Notification(email, "CANDIDATE", "Welcome to SmartRecruit",
							"Your candidate account is fully active. Complete your profile to match jobs.",
							NotificationType.GENERAL)
			);
		}

		List<Notification> saved = notificationRepository.saveAll(samples);
		return saved.stream().map(NotificationResponse::from).toList();
	}

	private Notification findAndVerifyOwnership(UUID id, String userEmail) {
		Notification notification = notificationRepository.findById(id)
				.orElseThrow(NotificationNotFoundException::new);
		
		if (!notification.getRecipientEmail().equalsIgnoreCase(normalizeEmail(userEmail))) {
			throw new ForbiddenNotificationAccessException("Notification does not belong to user");
		}
		return notification;
	}

	private static String normalizeEmail(String email) {
		return email != null ? email.trim().toLowerCase(Locale.ROOT) : "";
	}
}
