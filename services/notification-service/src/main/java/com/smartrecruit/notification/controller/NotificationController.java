package com.smartrecruit.notification.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smartrecruit.notification.dto.CreateNotificationRequest;
import com.smartrecruit.notification.dto.NotificationResponse;
import com.smartrecruit.notification.dto.UnreadCountResponse;
import com.smartrecruit.notification.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@Tag(name = "Notification Management", description = "Endpoints for recruiters and candidates to manage their notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
			summary = "View notification list",
			description = "US-78 / US-82: Retrieves all notifications for the authenticated recruiter or candidate, sorted by creation date descending."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Notification list retrieved"),
			@ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
	})
	public List<NotificationResponse> getNotifications(
			@AuthenticationPrincipal Jwt jwt,
			@Parameter(description = "Optional filter: true for read notifications, false for unread notifications")
			@RequestParam(required = false) Boolean read) {
		String userEmail = getUserEmail(jwt);
		return notificationService.getNotificationsForUser(userEmail, read);
	}

	@GetMapping("/unread-count")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Get unread notification count", description = "Returns the number of unread notifications for the authenticated user.")
	public UnreadCountResponse getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
		String userEmail = getUserEmail(jwt);
		return notificationService.getUnreadCount(userEmail);
	}

	@PatchMapping("/{id}/read")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
			summary = "Mark notification as read",
			description = "US-79 / US-83: Marks a specific notification belonging to the authenticated user as read."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Notification marked as read"),
			@ApiResponse(responseCode = "403", description = "Notification belongs to another user"),
			@ApiResponse(responseCode = "404", description = "Notification not found")
	})
	public NotificationResponse markAsRead(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID id) {
		String userEmail = getUserEmail(jwt);
		return notificationService.markAsRead(id, userEmail);
	}

	@PatchMapping("/{id}/unread")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
			summary = "Mark notification as unread",
			description = "US-80 / US-84: Marks a specific notification belonging to the authenticated user as unread."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Notification marked as unread"),
			@ApiResponse(responseCode = "403", description = "Notification belongs to another user"),
			@ApiResponse(responseCode = "404", description = "Notification not found")
	})
	public NotificationResponse markAsUnread(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID id) {
		String userEmail = getUserEmail(jwt);
		return notificationService.markAsUnread(id, userEmail);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
			summary = "Delete a notification",
			description = "US-81 / US-85: Permanently deletes a specific notification belonging to the authenticated user."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
			@ApiResponse(responseCode = "403", description = "Notification belongs to another user"),
			@ApiResponse(responseCode = "404", description = "Notification not found")
	})
	public void deleteNotification(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID id) {
		String userEmail = getUserEmail(jwt);
		notificationService.deleteNotification(id, userEmail);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Create a new notification",
			description = "Creates a notification for a candidate or recruiter (Used by system/services or Swagger testing)."
	)
	public NotificationResponse createNotification(@Valid @RequestBody CreateNotificationRequest request) {
		return notificationService.createNotification(request);
	}

	@PostMapping("/seed-sample")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Seed sample notifications for testing",
			description = "Generates sample notifications for the authenticated user (or specified target email) to easily test Swagger UI."
	)
	public List<NotificationResponse> seedSampleNotifications(
			@AuthenticationPrincipal Jwt jwt,
			@Parameter(description = "Role for sample notifications: CANDIDATE or RECRUITER", example = "RECRUITER")
			@RequestParam(defaultValue = "RECRUITER") String role,
			@Parameter(description = "Optional custom recipient email (if omitted, uses authenticated user email)")
			@RequestParam(required = false) String targetEmail) {
		String email = (targetEmail != null && !targetEmail.isBlank()) ? targetEmail : getUserEmail(jwt);
		return notificationService.seedSampleNotifications(email, role);
	}

	private static String getUserEmail(Jwt jwt) {
		if (jwt == null || jwt.getSubject() == null) {
			return "demo@smartrecruit.com";
		}
		return jwt.getSubject();
	}
}
