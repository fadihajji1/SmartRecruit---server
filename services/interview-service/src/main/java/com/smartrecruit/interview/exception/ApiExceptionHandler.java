package com.smartrecruit.interview.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ApiError(Instant timestamp, int status, String code, String message) {
        static ApiError of(HttpStatus status, String code, String message) {
            return new ApiError(Instant.now(), status.value(), code, message);
        }
    }

    @ExceptionHandler(InterviewNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError interviewNotFound(InterviewNotFoundException ex) {
        return ApiError.of(HttpStatus.NOT_FOUND, "INTERVIEW_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DelayRequestNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError delayRequestNotFound(DelayRequestNotFoundException ex) {
        return ApiError.of(HttpStatus.NOT_FOUND, "DELAY_REQUEST_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InvalidInterviewStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiError invalidState(InvalidInterviewStateException ex) {
        return ApiError.of(HttpStatus.CONFLICT, "INVALID_INTERVIEW_STATE", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError validation(MethodArgumentNotValidException ex) {
        FieldError error = ex.getBindingResult().getFieldError();
        return ApiError.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                error == null ? "Invalid request" : error.getDefaultMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError illegalArgument(IllegalArgumentException ex) {
        return ApiError.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    }
}
