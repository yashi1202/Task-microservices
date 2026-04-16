package com.smarttask.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey signingKey;
    
    @Value("${jwt.secret}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ─── Generate access token with client binding ────────────────────────

    public String generateToken(String username, String role,
                                 String clientIp, String userAgent) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        // Unique ID per token — used for blacklisting
        String tokenId = UUID.randomUUID().toString();

        // Fingerprint = hash of IP + UserAgent
        // Makes token unusable from a different device/browser
        String fingerprint = generateFingerprint(clientIp, userAgent);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role",        role);
        claims.put("clientIp",    clientIp);
        claims.put("fingerprint", fingerprint);
        claims.put("tokenId",     tokenId);

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    // ─── Generate refresh token ───────────────────────────────────────────

    public String generateRefreshToken(String username) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .claim("tokenId", UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    // ─── Validate token ───────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT unsupported: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT malformed: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("JWT signature invalid: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT empty: {}", ex.getMessage());
        }
        return false;
    }

    // ─── Validate token with client binding ───────────────────────────────

    /**
     * Validates token AND checks that the request comes from
     * the same client (IP + User-Agent) that originally logged in.
     * If someone steals the token and uses it from a different
     * device or IP, this check will reject it.
     */
    public boolean validateTokenWithClientBinding(String token,
            String currentIp, String currentUserAgent) {

        if (!validateToken(token)) return false;

        try {
            Claims claims = parseClaims(token);

            String tokenFingerprint =
                    claims.get("fingerprint", String.class);
            String currentFingerprint =
                    generateFingerprint(currentIp, currentUserAgent);

            if (!tokenFingerprint.equals(currentFingerprint)) {
                log.warn("Token fingerprint mismatch! " +
                        "Token from IP={} used from IP={}",
                        claims.get("clientIp"), currentIp);
                return false;
            }

            return true;

        } catch (Exception ex) {
            log.warn("Token binding validation failed: {}",
                    ex.getMessage());
            return false;
        }
    }

    // ─── Extract claims ───────────────────────────────────────────────────

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractTokenId(String token) {
        return parseClaims(token).get("tokenId", String.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    public long getRemainingExpiry(String token) {
        Date expiry = parseClaims(token).getExpiration();
        return expiry.getTime() - System.currentTimeMillis();
    }
    
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)   
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Creates a fingerprint from client IP and User-Agent.
     * Same client = same fingerprint every time.
     * Different device or IP = different fingerprint = rejected.
     */
    private String generateFingerprint(String clientIp,
                                        String userAgent) {
        String raw = clientIp + "|" + userAgent;
        return Integer.toHexString(raw.hashCode());
    }
}