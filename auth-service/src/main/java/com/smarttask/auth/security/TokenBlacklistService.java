package com.smarttask.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long>
            blacklist = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String BLACKLIST_PREFIX = "blacklist:";

 /*   public void blacklist(String tokenId,
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
    }*/
    
    
    public void blacklist(String token) {

        String tokenId = jwtTokenProvider.extractTokenId(token);
        Date expiration = jwtTokenProvider.extractExpiration(token);

        long expiryMs = expiration.getTime() - System.currentTimeMillis();

        if (expiryMs <= 0) {
            log.warn("Token already expired, skipping blacklist");
            return;
        }

        String key = BLACKLIST_PREFIX + tokenId;

        redisTemplate.opsForValue().set(
                key,
                "true",
                expiryMs,
                TimeUnit.MILLISECONDS
        );

        log.info("Token blacklisted in Redis: {}", tokenId);
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