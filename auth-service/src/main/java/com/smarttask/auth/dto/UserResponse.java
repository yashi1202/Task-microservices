package com.smarttask.auth.dto;

import java.time.LocalDateTime;

import com.smarttask.auth.enums.Role;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
	private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private boolean active;
    private String createdAt;
    private String updatedAt;

}
