package com.smartrecruit.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.smartrecruit.notification.dto.ApiError;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NotificationNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleNotFound(NotificationNotFoundException ex) {
		return ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
	}

	@ExceptionHandler(ForbiddenNotificationAccessException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiError handleForbidden(ForbiddenNotificationAccessException ex) {
		return ApiError.of(HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleValidationErrors(MethodArgumentNotValidException ex) {
		FieldError fieldError = ex.getBindingResult().getFieldError();
		String message = fieldError != null ? fieldError.getDefaultMessage() : "Invalid input parameters";
		return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", message);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleIllegalArgument(IllegalArgumentException ex) {
		return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handleGeneric(Exception ex) {
		return ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage());
	}
}
