package com.smarttask.auth.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutEvent {
    private String tokenId;
    private String sessionId;
    private String username;
    private long tokenExpiresAtEpochMs;
    private long occurredAtEpochMs;
}
