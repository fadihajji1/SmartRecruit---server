package com.smartrecruit.notification.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.smartrecruit.notification.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;

/** Every error leaves the service in the documented shape: timestamp, status, code, message, path. */
@RestControllerAdvice
public class ApiExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(NotificationNotFoundException.class)
	public ResponseEntity<ApiError> notFound(NotificationNotFoundException ex, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(ForbiddenNotificationAccessException.class)
	public ResponseEntity<ApiError> forbidden(ForbiddenNotificationAccessException ex, HttpServletRequest request) {
		return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request);
	}

	/** Method security (@PreAuthorize) failures must stay 403, not fall through to the generic 500 handler. */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> accessDenied(HttpServletRequest request) {
		return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have access to this resource", request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		FieldError field = ex.getBindingResult().getFieldError();
		String message = field != null ? field.getField() + ": " + field.getDefaultMessage() : "Request validation failed";
		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
	}

	@ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest request) {
		String message = ex instanceof MethodArgumentTypeMismatchException mismatch
				? "Invalid value for " + mismatch.getName() : ex.getMessage();
		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> unreadable(HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "The request body could not be read", request);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiError> status(ResponseStatusException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		return error(status, status.name(), ex.getReason() == null ? status.getReasonPhrase() : ex.getReason(), request);
	}

	/** Unexpected failures are logged with their stack trace; the caller gets a message without internal details. */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
		// Spring MVC's own exceptions (unknown path, wrong method, missing parameter) already carry their status
		if (ex instanceof org.springframework.web.ErrorResponse known) {
			HttpStatus status = HttpStatus.valueOf(known.getStatusCode().value());
			return error(status, status.name(), known.getBody().getDetail() == null ? status.getReasonPhrase() : known.getBody().getDetail(), request);
		}
		log.error("Unhandled error on {} {}", request.getMethod(), request.getRequestURI(), ex);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Something went wrong on our side. Please try again.", request);
	}

	private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message, HttpServletRequest request) {
		return ResponseEntity.status(status).body(ApiError.of(status.value(), code, message, request.getRequestURI()));
	}
}
