package com.smartrecruit.recruitment.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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

/**
 * Turns every failure into the documented error contract:
 * {@code {"timestamp", "status", "code", "message", "path"}}.
 * Codes are stable identifiers the frontend can branch on; messages are written for people.
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ApiError(Instant timestamp, int status, String code, String message, String path) {
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> status(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        return error(status, codeFor(status), message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        FieldError field = ex.getBindingResult().getFieldError();
        String message = field == null ? "Request validation failed" : field.getField() + ": " + field.getDefaultMessage();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid value for " + ex.getName(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "The request body could not be read", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiError> conflict(IllegalStateException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "INVALID_STATE", ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "CONFLICT", "This change conflicts with existing data", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> accessDenied(HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have access to this resource", request);
    }

    private static String codeFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "VALIDATION_ERROR";
            case NOT_FOUND -> "NOT_FOUND";
            case FORBIDDEN -> "ACCESS_DENIED";
            case CONFLICT -> "CONFLICT";
            case UNAUTHORIZED -> "AUTHENTICATION_REQUIRED";
            default -> status.name();
        };
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
            HttpServletRequest request) {
        if (status.is5xxServerError()) log.warn("{} {} -> {} {}", request.getMethod(), request.getRequestURI(), status.value(), message);
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
