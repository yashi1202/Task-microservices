package com.smarttask.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionData implements Serializable {

    private String        sessionId;
    private String        tokenId;
    private String        username;
    private String        role;
    private String        ipAddress;
    private String        userAgent;
    private String        deviceType;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime expiresAt;
    private boolean       active;
}