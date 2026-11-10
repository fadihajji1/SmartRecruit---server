package com.smartrecruit.notification.exception;

public class ForbiddenNotificationAccessException extends RuntimeException {
	public ForbiddenNotificationAccessException() {
		super("Access denied for this notification");
	}

	public ForbiddenNotificationAccessException(String message) {
		super(message);
	}
}
