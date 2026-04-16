package com.smarttask.auth.service.impl;

import com.smarttask.auth.dto.JwtAuthResponse;
import com.smarttask.auth.dto.LoginRequest;
import com.smarttask.auth.entity.User;
import com.smarttask.auth.exception
        .AccountDisabledException;
import com.smarttask.auth.messaging.LogoutEvent;
import com.smarttask.auth.messaging.LogoutEventPublisher;
import com.smarttask.auth.exception
        .UnauthorizedException;
import com.smarttask.auth.model.SessionData;
import com.smarttask.auth.repository.UserRepository;
import com.smarttask.auth.security
        .JwtTokenProvider;
import com.smarttask.auth.security
        .TokenBlacklistService;
import com.smarttask.auth.service.AuthService;
import com.smarttask.auth.utility.UserMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core
        .RedisTemplate;
import org.springframework.security.crypto.password
        .PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation
        .Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl
        implements AuthService {

    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final JwtTokenProvider   jwtTokenProvider;
    private final TokenBlacklistService
            tokenBlacklistService;
    private final RedisTemplate<String, Object>
            redisTemplate;
    private final UserMapper userMapper;
    private final LogoutEventPublisher logoutEventPublisher;

    // ─── Redis key prefixes ───────────────────────

    private static final String SESSION_PREFIX
            = "session:";
    private static final String USER_SESSIONS_PREFIX
            = "user_sessions:";
    private static final long SESSION_TTL_MINUTES
            = 30;

    // ─── Login ────────────────────────────────────

    @Override
    @Transactional
    public JwtAuthResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest) {

        // Find user
        User user = userRepository
                .findByUsername(
                        request.getUsername())
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Invalid username "
                                + "or password"));

        // Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            throw new UnauthorizedException(
                    "Invalid username or password");
        }

        // Check account is active
        if (!user.isActive()) {
            throw new AccountDisabledException(
                    "Your account has been disabled");
        }

        // Get client info
        String ipAddress =
                getClientIp(httpRequest);
        String userAgent =
                httpRequest.getHeader("User-Agent");

        // Generate JWT
        String token =
                jwtTokenProvider.generateToken(
                        user.getUsername(),
                        user.getRole().name(),
                        ipAddress,
                        userAgent);

        // Extract tokenId from JWT
        String tokenId =
                jwtTokenProvider
                        .extractTokenId(token);

        // Generate unique session ID
        String sessionId =
                UUID.randomUUID().toString();

        // Create session in Redis
        createSession(
                sessionId,
                tokenId,
                user.getUsername(),
                user.getRole().name(),
                ipAddress,
                userAgent);

        log.info("User '{}' logged in from IP={}",
                user.getUsername(), ipAddress);

        return JwtAuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .sessionId(sessionId)
                .username(user.getUsername())
                .role(user.getRole().name())
                .expiresIn(
                        jwtTokenProvider
                                .getExpirationMs())
                .build();
    }

    // ─── Logout ───────────────────────────────────

    @Override
    public void logout(
            String token,
            String sessionId) {
        String tokenId = jwtTokenProvider
                .extractTokenId(token);
        String username = jwtTokenProvider
                .extractUsername(token);
        long tokenExpiresAt = jwtTokenProvider
                .extractExpiration(token)
                .getTime();

        // Blacklist JWT
        tokenBlacklistService.blacklist(token);

        // Invalidate session
        if (sessionId != null
                && !sessionId.isBlank()) {
            invalidateSession(sessionId);
        }

        logoutEventPublisher.publish(
                LogoutEvent.builder()
                        .tokenId(tokenId)
                        .sessionId(sessionId)
                        .username(username)
                        .tokenExpiresAtEpochMs(
                                tokenExpiresAt)
                        .occurredAtEpochMs(
                                System.currentTimeMillis())
                        .build());

        log.info("User logged out, "
                + "session={}", sessionId);
    }

    // ─── Session — check valid ────────────────────

    @Override
    public boolean isSessionValid(
            String sessionId) {
        if (sessionId == null
                || sessionId.isBlank()) {
            return false;
        }
        SessionData session =
                getSession(sessionId);
        return session != null
                && session.isActive();
    }

    // ─── Session — refresh activity ───────────────

    @Override
    public void refreshSession(
            String sessionId) {
        SessionData session =
                getSession(sessionId);
        if (session == null) return;

        session.setLastActivityAt(
                LocalDateTime.now());
        session.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(
                                SESSION_TTL_MINUTES));

        redisTemplate.opsForValue().set(
                SESSION_PREFIX + sessionId,
                session,
                SESSION_TTL_MINUTES,
                TimeUnit.MINUTES);
    }

    // ─── Session — get my sessions ────────────────

    @Override
    public List<SessionData> getUserSessions(
            String username) {
        Set<Object> sessionIds =
                redisTemplate.opsForSet()
                        .members(
                            USER_SESSIONS_PREFIX
                            + username);

        if (sessionIds == null
                || sessionIds.isEmpty()) {
            return List.of();
        }

        List<SessionData> sessions =
                new ArrayList<>();
        for (Object id : sessionIds) {
            SessionData session =
                    getSession(id.toString());
            if (session != null
                    && session.isActive()) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    // ─── Private — create session ─────────────────

    private void createSession(
            String sessionId,
            String tokenId,
            String username,
            String role,
            String ipAddress,
            String userAgent) {

        SessionData session = SessionData.builder()
                .sessionId(sessionId)
                .tokenId(tokenId)
                .username(username)
                .role(role)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceType(
                        detectDeviceType(userAgent))
                .createdAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now()
                        .plusMinutes(
                                SESSION_TTL_MINUTES))
                .active(true)
                .build();

        // Store session
        redisTemplate.opsForValue().set(
                SESSION_PREFIX + sessionId,
                session,
                SESSION_TTL_MINUTES,
                TimeUnit.MINUTES);

        // Add to user session set
        redisTemplate.opsForSet().add(
                USER_SESSIONS_PREFIX + username,
                sessionId);
        redisTemplate.expire(
                USER_SESSIONS_PREFIX + username,
                SESSION_TTL_MINUTES + 5,
                TimeUnit.MINUTES);

        log.info("Session created for '{}' "
                + "sessionId={} ip={}",
                username, sessionId, ipAddress);
    }

    // ─── Private — get session ────────────────────

    private SessionData getSession(
            String sessionId) {
        return (SessionData) redisTemplate
                .opsForValue()
                .get(SESSION_PREFIX + sessionId);
    }

    // ─── Private — invalidate session ────────────

    private void invalidateSession(
            String sessionId) {
        SessionData session =
                getSession(sessionId);
        if (session == null) return;

        redisTemplate.delete(
                SESSION_PREFIX + sessionId);
        redisTemplate.opsForSet().remove(
                USER_SESSIONS_PREFIX
                        + session.getUsername(),
                sessionId);

        log.info("Session invalidated: {}",
                sessionId);
    }

    // ─── Private — helpers ────────────────────────

    private String getClientIp(
            HttpServletRequest request) {
        String xfHeader =
                request.getHeader(
                        "X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String detectDeviceType(
            String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile")
                || ua.contains("android")) {
            return "Mobile";
        } else if (ua.contains("tablet")
                || ua.contains("ipad")) {
            return "Tablet";
        }
        return "Desktop";
    }
}
