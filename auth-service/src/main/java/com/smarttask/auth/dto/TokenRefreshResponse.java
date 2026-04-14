package com.smarttask.auth.dto;
 import lombok.*;
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default private String tokenType = "Bearer";
    private long expiresIn;
}