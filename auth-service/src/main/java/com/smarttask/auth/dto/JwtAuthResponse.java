package com.smarttask.auth.dto;
import com.smarttask.auth.enums.Role;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class JwtAuthResponse {
	
	private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private long expiresIn;

}