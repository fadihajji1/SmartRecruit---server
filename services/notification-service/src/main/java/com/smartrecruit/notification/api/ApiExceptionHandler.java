package com.smartrecruit.notification.api;

import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ErrorBody(Instant timestamp, int status, String code, String message, String path) {}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(e -> e.getField() + " " + e.getDefaultMessage()).orElse("Request validation failed");
        return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, req);
    }
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    ResponseEntity<ErrorBody> badRequest(Exception ex, HttpServletRequest req) { return body(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req); }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorBody> status(ResponseStatusException ex, HttpServletRequest req) { HttpStatus s=HttpStatus.valueOf(ex.getStatusCode().value()); return body(s,s.name(),ex.getReason()==null?s.getReasonPhrase():ex.getReason(),req); }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorBody> forbidden(HttpServletRequest req) { return body(HttpStatus.FORBIDDEN,"ACCESS_DENIED","Access denied",req); }
    private ResponseEntity<ErrorBody> body(HttpStatus status,String code,String message,HttpServletRequest req) { return ResponseEntity.status(status).body(new ErrorBody(Instant.now(),status.value(),code,message,req.getRequestURI())); }
}
