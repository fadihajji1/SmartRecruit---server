package com.smartrecruit.notification.dto;

import com.smartrecruit.notification.model.NotificationType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
		@NotBlank(message = "Recipient email is required")
		@Email(message = "Recipient email must be valid")
		String recipientEmail,

		@Size(max = 32, message = "Recipient role must be at most 32 characters")
		String recipientRole,

		@NotBlank(message = "Title is required")
		@Size(max = 200, message = "Title must be at most 200 characters")
		String title,

		@NotBlank(message = "Message is required")
		@Size(max = 2000, message = "Message must be at most 2000 characters")
		String message,

		NotificationType type
) {
}
