package com.smarttask.gateway.filter;

import com.smarttask.gateway.util.JwtUtil;
import com.smarttask.gateway.security.RevocationStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory
        .AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive
        .ServerHttpRequest;
import org.springframework.http.server.reactive
        .ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends
        AbstractGatewayFilterFactory
                <JwtAuthFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RevocationStore revocationStore;

    // Hardcoded public paths — no @Value injection needed
    private static final List<String> PUBLIC_PATHS =
            List.of(
                "/api/auth/login"
            );

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest()
                    .getURI().getPath();

            // Skip JWT for public endpoints
            if (isPublicPath(path)) {
                log.debug("Public path — skipping JWT: {}",
                        path);
                return chain.filter(exchange);
            }

            // Check Authorization header
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null
                    || !authHeader
                            .startsWith("Bearer ")) {
                log.warn("Missing Authorization header "
                        + "for path: {}", path);
                return unauthorizedResponse(exchange,
                        "Missing or invalid "
                        + "Authorization header");
            }

            // Extract and validate token
            String token = authHeader.substring(7);

            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT for path: {}",
                        path);
                return unauthorizedResponse(exchange,
                        "Invalid or expired token");
            }

            // Extract username and role
            String username =
                    jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);
            String tokenId = jwtUtil.extractTokenId(token);

            String tokenBlacklistKey =
                    "blacklist:" + tokenId;
            Boolean tokenRevokedInRedis =
                    redisTemplate.hasKey(
                            tokenBlacklistKey);
            boolean tokenRevokedInMemory =
                    revocationStore
                            .isTokenRevoked(tokenId);

            if (Boolean.TRUE.equals(
                    tokenRevokedInRedis)
                    || tokenRevokedInMemory) {
                log.warn("Rejected revoked token "
                        + "tokenId={} user={}",
                        tokenId, username);
                return unauthorizedResponse(
                        exchange,
                        "Token has been revoked. "
                                + "Please login again.");
            }

            // ─── Session validation ───────────────────
            // NEW — get sessionId from request header
            String sessionId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Session-Id");

            // NEW — validate session in Redis
            if (sessionId == null
                    || sessionId.isBlank()) {
                return unauthorizedResponse(
                        exchange,
                        "Missing session id. "
                                + "Please login again.");
            }

            if (revocationStore
                    .isSessionRevoked(sessionId)) {
                log.warn("Rejected revoked session "
                        + "user={} sessionId={}",
                        username, sessionId);
                return unauthorizedResponse(
                        exchange,
                        "Session expired. "
                                + "Please login again.");
            }

            {

                Boolean sessionExists =
                        redisTemplate.hasKey(
                                "session:" + sessionId);

                if (sessionExists == null
                        || !sessionExists) {
                    log.warn("Session expired or "
                            + "not found for user={} "
                            + "sessionId={}",
                            username, sessionId);
                    return unauthorizedResponse(
                            exchange,
                            "Session expired. "
                            + "Please login again.");
                }

                // Refresh session TTL — resets
                // the 30 min inactivity timer
                redisTemplate.expire(
                        "session:" + sessionId,
                        30,
                        java.util.concurrent
                                .TimeUnit.MINUTES);

                log.debug("Session valid and "
                        + "refreshed for user={}",
                        username);
            }
            // ─────────────────────────────────────────

            log.debug("JWT valid — user={} role={} "
                    + "path={}", username, role, path);

            // Inject headers for downstream services
            ServerHttpRequest mutatedRequest =
                    exchange.getRequest().mutate()
                            .header("X-Auth-Username",
                                    username)
                            .header("X-Auth-Role",
                                    role)
                            // NEW — forward sessionId
                            // to downstream services
                            .header("X-Session-Id",
                                    sessionId != null
                                    ? sessionId : "")
                            .build();

            ServerWebExchange mutatedExchange =
                    exchange.mutate()
                            .request(mutatedRequest)
                            .build();

            return chain.filter(mutatedExchange);
        };
    }
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(path::equals);
    }

    private Mono<Void> unauthorizedResponse(
            ServerWebExchange exchange,
            String message) {

        ServerHttpResponse response =
                exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(
                MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"success\":false,"
                + "\"status\":401,"
                + "\"error\":\"Unauthorized\","
                + "\"message\":\"%s\"}",
                message);

        return response.writeWith(Mono.just(
                response.bufferFactory()
                        .wrap(body.getBytes())));
    }

    public static class Config {
        // No config needed
    }
}
