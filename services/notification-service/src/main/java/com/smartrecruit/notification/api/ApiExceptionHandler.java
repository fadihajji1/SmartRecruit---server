package com.smartrecruit.notification.api;

import java.time.Instant;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ErrorBody(Instant timestamp, int status, String message) {}

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, MethodArgumentNotValidException.class})
    ResponseEntity<ErrorBody> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(new ErrorBody(Instant.now(), 400, ex.getMessage()));
    }

    @ExceptionHandler({SecurityException.class, AccessDeniedException.class})
    ResponseEntity<ErrorBody> forbidden(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorBody(Instant.now(), 403, "Access denied"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ErrorBody> notFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorBody(Instant.now(), 404, "Resource not found"));
    }
}
