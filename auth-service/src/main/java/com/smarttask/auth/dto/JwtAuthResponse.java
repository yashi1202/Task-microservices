package com.smarttask.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtAuthResponse {

    private final String  accessToken;
    private final String  tokenType;
    private final String  sessionId;    // ← add this
    private final String  username;
    private final String  role;
    private final long    expiresIn;

    @JsonCreator
    public JwtAuthResponse(
            @JsonProperty("accessToken")
                    String accessToken,
            @JsonProperty("tokenType")
                    String tokenType,
            @JsonProperty("sessionId")
                    String sessionId,
            @JsonProperty("username")
                    String username,
            @JsonProperty("role")
                    String role,
            @JsonProperty("expiresIn")
                    long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType   = tokenType;
        this.sessionId   = sessionId;
        this.username    = username;
        this.role        = role;
        this.expiresIn   = expiresIn;
    }
}