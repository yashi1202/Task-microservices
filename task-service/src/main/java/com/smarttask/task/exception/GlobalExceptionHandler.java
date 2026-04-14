package com.smarttask.task.exception;

import com.smarttask.task.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 404 Not Found ────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        404,
                        "Not Found",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // ─── 400 Bad Request ──────────────────────────────────────────────────

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {

        log.warn("Bad request: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        400,
                        "Bad Request",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // ─── 403 Forbidden ────────────────────────────────────────────────────

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request) {

        log.warn("Forbidden: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(
                        403,
                        "Forbidden",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // ─── 409 Task already assigned ────────────────────────────────────────

    @ExceptionHandler(TaskAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyAssigned(
            TaskAlreadyAssignedException ex,
            HttpServletRequest request) {

        log.warn("Task already assigned: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        409,
                        "Conflict",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // ─── 400 Task not assigned ────────────────────────────────────────────

    @ExceptionHandler(TaskNotAssignedException.class)
    public ResponseEntity<ErrorResponse> handleNotAssigned(
            TaskNotAssignedException ex,
            HttpServletRequest request) {

        log.warn("Task not assigned: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        400,
                        "Bad Request",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // ─── 409 Optimistic locking ───────────────────────────────────────────

    @ExceptionHandler({
        OptimisticLockException.class,
        ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Optimistic lock conflict: {}",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        409,
                        "Conflict",
                        "This task was modified by another user. "
                        + "Please refresh and try again.",
                        request.getRequestURI()));
    }

    // ─── 400 Validation errors (@Valid) ───────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError :
                ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage());
        }

        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.withFieldErrors(
                        400,
                        "Validation Failed",
                        "One or more fields have invalid values",
                        request.getRequestURI(),
                        fieldErrors));
    }

    // ─── 400 Type mismatch ────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format(
                "Invalid value '%s' for parameter '%s'. "
                + "Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "unknown");

        log.warn("Type mismatch: {}", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        400,
                        "Bad Request",
                        message,
                        request.getRequestURI()));
    }

    // ─── 400 Missing header ───────────────────────────────────────────────

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(
            MissingRequestHeaderException ex,
            HttpServletRequest request) {

        String message = "Required header '"
                + ex.getHeaderName() + "' is missing";

        log.warn("Missing header: {}", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        400,
                        "Bad Request",
                        message,
                        request.getRequestURI()));
    }

    // ─── 500 Catch-all ────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error at {}: {}",
                request.getRequestURI(),
                ex.getMessage(), ex);

        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        500,
                        "Internal Server Error",
                        "An unexpected error occurred. "
                        + "Please contact support.",
                        request.getRequestURI()));
    }
}