package com.smarttask.auth.controller;

import com.smarttask.auth.dto.ApiResponse;
import com.smarttask.auth.dto.*;
import com.smarttask.auth.enums.Role;
import com.smarttask.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─── Public self-registration ─────────────────────────────────────────
    // No token required — role always forced to ROLE_USER

    /*@PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> selfRegister(
            @Valid @RequestBody RegisterUserRequest request) {

        // Force ROLE_USER — cannot self-register as ADMIN or MANAGER
        request.setRole(Role.ROLE_USER);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful",
                        userService.createUser(request)));
    }*/

    // ─── Admin creates user with any role ─────────────────────────────────
    // Requires ADMIN token from gateway header

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
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
                        userService.createUser(request,role)));
    }

    // ─── Get all users ────────────────────────────────────────────────────
    // ADMIN and MANAGER only

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestHeader(value = "X-Auth-Role",
                    required = false) String role) {

        if (!"ROLE_ADMIN".equals(role)
                && !"ROLE_MANAGER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "Only ADMIN or MANAGER can view all users"));
        }

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page,
                Math.min(size, 100), sort);

        return ResponseEntity.ok(ApiResponse.success(
                userService.getAllUsers(pageable)));
    }

    // ─── Get user by ID ───────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Username",
                    required = false) String callerUsername,
            @RequestHeader(value = "X-Auth-Role",
                    required = false) String role) {

        UserResponse user = userService.getUserById(id);

        // Users can only see their own profile
        // ADMIN and MANAGER can see anyone
        if (!"ROLE_ADMIN".equals(role)
                && !"ROLE_MANAGER".equals(role)
                && !user.getUsername().equals(callerUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "You can only view your own profile"));
        }

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // ─── Update user ──────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader(value = "X-Auth-Username",
                    required = false) String callerUsername,
            @RequestHeader(value = "X-Auth-Role",
                    required = false) String role) {

        UserResponse existing = userService.getUserById(id);

        // Only ADMIN or the user themselves can update
        if (!"ROLE_ADMIN".equals(role)
                && !existing.getUsername().equals(callerUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "You can only update your own profile"));
        }

        // Only ADMIN can change roles
        if (request.getRole() != null
                && !"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "Only ADMIN can change user roles"));
        }

        return ResponseEntity.ok(ApiResponse.success(
                "User updated successfully",
                userService.updateUser(id, request)));
    }

    // ─── Change password ──────────────────────────────────────────────────

    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "X-Auth-Username",
                    required = false) String callerUsername) {

        UserResponse existing = userService.getUserById(id);

        if (!existing.getUsername().equals(callerUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "You can only change your own password"));
        }

        userService.changePassword(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully",
                        null));
    }

    // ─── Enable / Disable user ────────────────────────────────────────────

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(
            @PathVariable Long id,
            @RequestParam boolean active,
            @RequestHeader(value = "X-Auth-Role",
                    required = false) String role) {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "Only ADMIN can enable or disable users"));
        }
        return ResponseEntity.ok(ApiResponse.success(
                "User status updated",
                userService.toggleActive(id, active)));
    }

    // ─── Delete user ──────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Role",
                    required = false) String role) {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "Only ADMIN can delete users"));
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully",
                        null));
    }
}