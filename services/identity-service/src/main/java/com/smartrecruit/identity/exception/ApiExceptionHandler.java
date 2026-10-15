package com.smartrecruit.identity.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.smartrecruit.identity.dto.ApiError;
import com.smartrecruit.identity.service.EmailAlreadyRegisteredException;
import com.smartrecruit.identity.service.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
	
	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	ResponseEntity<ApiError> emailConflict(EmailAlreadyRegisteredException exception, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", exception.getMessage(), request);
	}

	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<ApiError> authenticationFailure(HttpServletRequest request) {
		return error(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Invalid credentials or inactive account",
				request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> validationFailure(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.findFirst()
				.orElse("Request validation failed");
		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ApiError> invalidRequest(IllegalArgumentException exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request);
	}

	@ExceptionHandler(UserNotFoundException.class)
	ResponseEntity<ApiError> userNotFound(UserNotFoundException exception, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", exception.getMessage(), request);
	}

	@ExceptionHandler(StorageException.class)
	ResponseEntity<ApiError> storageFailure(StorageException exception, HttpServletRequest request) {
		logger.error("Employment proof storage operation failed", exception);
		return error(HttpStatus.BAD_GATEWAY, "FILE_STORAGE_ERROR", "Employment proof could not be stored", request);
	}

	private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
			HttpServletRequest request) {
		return ResponseEntity.status(status)
				.body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
	}
}
