package com.smarttask.gateway.messaging;

import lombok.Data;

@Data
public class LogoutEvent {
    private String tokenId;
    private String sessionId;
    private String username;
    private long tokenExpiresAtEpochMs;
    private long occurredAtEpochMs;
}
