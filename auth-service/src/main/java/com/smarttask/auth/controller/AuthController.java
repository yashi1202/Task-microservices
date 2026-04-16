package com.smarttask.auth.controller;

import com.smarttask.auth.dto.*;
import com.smarttask.auth.enums.Role;
import com.smarttask.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─── 1. Login ─────────────────────────────────────────────────────────

    @PostMapping("/api/auth/login")
    public ResponseEntity<
            ApiResponse<JwtAuthResponse>> login(
                    @Valid @RequestBody
                            LoginRequest request,
                    HttpServletRequest
                            httpRequest) {

        JwtAuthResponse response =
                authService.login(
                        request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        response));
    }
    // ─── 2. Logout ────────────────────────────────────────────────────────

    @PostMapping("/api/auth/logout")
    public ResponseEntity<ApiResponse<Void>>
    logout(
            @RequestHeader(
                "Authorization")
                String authHeader,
            @RequestHeader(
                value = "X-Session-Id",
                required = false)
                String sessionId) {

String token = authHeader
        .substring(7);

authService.logout(token, sessionId);

return ResponseEntity.ok(
        ApiResponse.success(
                "Logged out successfully",
                null));
}
   
    // ─── 3. Public self-registration ──────────────────────────────────────
    // Anyone can call this — but role is always forced to ROLE_USER
    // so no one can self-register as ADMIN or MANAGER

   /* @PostMapping("/api/auth/register")
    public ResponseEntity<ApiResponse<UserResponse>> selfRegister(
            @Valid @RequestBody RegisterUserRequest request) {

        // Force role to USER — ignore whatever role was sent
        request.setRole(Role.ROLE_USER);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful",
                        authService.register(request)));
    }*/

    // ─── 4. Admin creates user (any role) ─────────────────────────────────
    // Only ADMIN can call this — allows creating MANAGER or ADMIN accounts

    /*@PostMapping("/api/users")
    public ResponseEntity<ApiResponse<UserResponse>> adminCreateUser(
            @Valid @RequestBody RegisterUserRequest request,
            @RequestHeader(value = "X-Auth-Role",
                    required = false) String role) {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "Only ADMIN can create users with any role"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User created successfully",
                        authService.register(request)));
    }*/

    // ─── Helper ───────────────────────────────────────────────────────────

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}