package com.smarttask.gateway.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RevocationStore {

    private final Map<String, Long> revokedTokenIds = new ConcurrentHashMap<>();
    private final Map<String, Long> revokedSessionIds = new ConcurrentHashMap<>();

    public void revokeToken(String tokenId, long expiresAtEpochMs) {
        if (tokenId != null && !tokenId.isBlank()) {
            revokedTokenIds.put(tokenId, expiresAtEpochMs);
        }
    }

    public void revokeSession(String sessionId, long expiresAtEpochMs) {
        if (sessionId != null && !sessionId.isBlank()) {
            revokedSessionIds.put(sessionId, expiresAtEpochMs);
        }
    }

    public boolean isTokenRevoked(String tokenId) {
        return isRevoked(revokedTokenIds, tokenId);
    }

    public boolean isSessionRevoked(String sessionId) {
        return isRevoked(revokedSessionIds, sessionId);
    }

    private boolean isRevoked(Map<String, Long> store, String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        Long expiresAt = store.get(key);
        if (expiresAt == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiresAt) {
            store.remove(key);
            return false;
        }
        return true;
    }
}
