package com.smarttask.auth.utility;



import com.smarttask.auth.dto.UserResponse;
import com.smarttask.auth.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(formatDate(user.getCreatedAt()))
                .build();
    }
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}