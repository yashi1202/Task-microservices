package com.smarttask.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String  message;
    private T       data;
    private String  error;

    public static <T> ApiResponse<T> success(
            String msg, T data) {
        return ApiResponse.<T>builder()
                .success(true).message(msg).data(data)
                .build();
    }
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true).data(data).build();
    }
    public static <T> ApiResponse<T> error(String err) {
        return ApiResponse.<T>builder()
                .success(false).error(err).build();
    }
}