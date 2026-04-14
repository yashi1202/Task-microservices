package com.smarttask.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int     status;
    private String  error;
    private String  message;
    private String  path;
    private String  timestamp;

    // For validation errors — field name → error message
    private Map<String, String> fieldErrors;

    public static ErrorResponse of(int status,
                                    String error,
                                    String message,
                                    String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now().format(
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    public static ErrorResponse withFieldErrors(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now().format(
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}