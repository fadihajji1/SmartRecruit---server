package com.smartrecruit.notification.dto;

import java.time.Instant;

/** The documented error contract shared by every SmartRecruit service. */
public record ApiError(Instant timestamp, int status, String code, String message, String path) {

	public static ApiError of(int status, String code, String message, String path) {
		return new ApiError(Instant.now(), status, code, message, path);
	}
}
