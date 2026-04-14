package com.smarttask.notification.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
            handleGeneric(
                    Exception ex,
                    HttpServletRequest request) {

        log.error("Unexpected error at {}: {}",
                request.getRequestURI(),
                ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message",
                            "An unexpected error occurred.",
                        "path", request.getRequestURI(),
                        "timestamp", LocalDateTime.now()
                                .format(DateTimeFormatter
                                        .ISO_LOCAL_DATE_TIME)
                ));
    }
}