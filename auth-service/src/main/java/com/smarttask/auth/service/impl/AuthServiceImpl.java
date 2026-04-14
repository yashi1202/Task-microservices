package com.smarttask.auth.service.impl;

import com.smarttask.auth.dto.*;
import com.smarttask.auth.dto.JwtAuthResponse;
import com.smarttask.auth.dto.LoginRequest;
import com.smarttask.auth.dto.RegisterUserRequest;
import com.smarttask.auth.dto.UserResponse;
import com.smarttask.auth.entity.User;
import com.smarttask.auth.exception.AccountDisabledException;
import com.smarttask.auth.exception.BadRequestException;
import com.smarttask.auth.exception.ConflictException;
import com.smarttask.auth.exception.UnauthorizedException;
import com.smarttask.auth.repository.UserRepository;
import com.smarttask.auth.security.JwtTokenProvider;
import com.smarttask.auth.security.TokenBlacklistService;
import com.smarttask.auth.security.TokenBlacklistService;
import com.smarttask.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      jwtTokenProvider;
    private final TokenBlacklistService blacklistService;

    // ─── Login ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public JwtAuthResponse login(LoginRequest request,
                                  String clientIp,
                                  String userAgent) {

        // Find user — same message for both not found and wrong
        // password to prevent username enumeration attacks
        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException(
                        "Invalid username or password"));

        // Verify password
        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(
                    "Invalid username or password");
        }

        // Check account is active
        if (!user.isActive()) {
            throw new AccountDisabledException(
                    "Your account has been disabled. "
                    + "Please contact support.");
        }

        // Generate JWT token bound to this client
        String token = jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getRole().name(),
                clientIp,
                userAgent);

        // Generate refresh token
        String refreshToken = jwtTokenProvider
                .generateRefreshToken(user.getUsername());

        log.info("User '{}' logged in from IP={}",
                user.getUsername(), clientIp);

        return JwtAuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .build();
    }

    // ─── Logout ───────────────────────────────────────────────────────────

    @Override
    public void logout(String token) {

        // Validate token before blacklisting
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadRequestException(
                    "Invalid or expired token");
        }

        String tokenId =
                jwtTokenProvider.extractTokenId(token);
        long remainingExpiry =
                jwtTokenProvider.getRemainingExpiry(token);

        // Add to Redis blacklist for remaining lifetime
        blacklistService.blacklist(tokenId, remainingExpiry);

        log.info("Token blacklisted for user '{}'",
                jwtTokenProvider.extractUsername(token));
    }

    // ─── Register ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {

        // Check username not already taken
        if (userRepository.existsByUsername(
                request.getUsername())) {
            throw new ConflictException(
                    "Username '" + request.getUsername()
                    + "' is already taken");
        }

        // Check email not already registered
        if (userRepository.existsByEmail(
                request.getEmail())) {
            throw new ConflictException(
                    "Email '" + request.getEmail()
                    + "' is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(
                        request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("User '{}' registered with role {}",
                saved.getUsername(), saved.getRole());

        return toResponse(saved);
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt() != null
                        ? user.getCreatedAt().format(
                                DateTimeFormatter
                                        .ISO_LOCAL_DATE_TIME)
                        : null)
                .build();
    }
}