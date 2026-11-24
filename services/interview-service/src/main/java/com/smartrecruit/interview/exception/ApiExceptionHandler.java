package com.smartrecruit.interview.exception;

import java.time.Instant;

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

import jakarta.servlet.http.HttpServletRequest;

/** Every error leaves the service in the documented shape: timestamp, status, code, message, path. */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ApiError(Instant timestamp, int status, String code, String message, String path) {
    }

    @ExceptionHandler(InterviewNotFoundException.class)
    ResponseEntity<ApiError> interviewNotFound(InterviewNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "INTERVIEW_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DelayRequestNotFoundException.class)
    ResponseEntity<ApiError> delayRequestNotFound(DelayRequestNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "DELAY_REQUEST_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidInterviewStateException.class)
    ResponseEntity<ApiError> invalidState(InvalidInterviewStateException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "INVALID_INTERVIEW_STATE", ex.getMessage(), request);
    }

    @ExceptionHandler(DependencyUnavailableException.class)
    ResponseEntity<ApiError> dependencyUnavailable(DependencyUnavailableException ex, HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        FieldError field = ex.getBindingResult().getFieldError();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                field == null ? "Request validation failed" : field.getField() + ": " + field.getDefaultMessage(), request);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest request) {
        String message = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? "Invalid value for " + mismatch.getName() : ex.getMessage();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "The request body could not be read", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> accessDenied(HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have access to this resource", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> status(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return error(status, status.name(), ex.getReason() == null ? status.getReasonPhrase() : ex.getReason(), request);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
            HttpServletRequest request) {
        if (status.is5xxServerError()) log.warn("{} {} -> {} {}", request.getMethod(), request.getRequestURI(), status.value(), message);
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
