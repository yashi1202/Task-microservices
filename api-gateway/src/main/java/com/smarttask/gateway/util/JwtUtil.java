package com.smarttask.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Validate JWT token
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT unsupported: {}",
                    ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT malformed: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("JWT signature invalid: {}",
                    ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT empty: {}", ex.getMessage());
        }
        return false;
    }

    // Extract username from token
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // Extract role from token
    public String extractRole(String token) {
        return parseClaims(token)
                .get("role", String.class);
    }

    public String extractTokenId(String token) {
        return parseClaims(token)
                .get("tokenId", String.class);
    }

    // Parse and return claims
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
