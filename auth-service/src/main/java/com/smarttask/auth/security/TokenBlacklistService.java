package com.smarttask.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long>
            blacklist = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public void blacklist(String tokenId,
                           long expiryMs) {
        long expiryTime = System.currentTimeMillis()
                + expiryMs;
        blacklist.put(tokenId, expiryTime);

        // Auto-remove after expiry
        scheduler.schedule(
                () -> blacklist.remove(tokenId),
                expiryMs,
                TimeUnit.MILLISECONDS);

        log.info("Token blacklisted: {}", tokenId);
    }

    public boolean isBlacklisted(String tokenId) {
        Long expiryTime = blacklist.get(tokenId);
        if (expiryTime == null) return false;

        if (System.currentTimeMillis() > expiryTime) {
            blacklist.remove(tokenId);
            return false;
        }
        return true;
    }
}