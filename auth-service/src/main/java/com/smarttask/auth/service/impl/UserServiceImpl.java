package com.smarttask.auth.service.impl;

import com.smarttask.auth.dto.ChangePasswordRequest;
import com.smarttask.auth.dto.RegisterUserRequest;
import com.smarttask.auth.dto.UpdateUserRequest;
import com.smarttask.auth.dto.UserResponse;
import com.smarttask.auth.entity.User;
import com.smarttask.auth.enums.Role;
import com.smarttask.auth.exception.BadRequestException;
import com.smarttask.auth.exception.ConflictException;
import com.smarttask.auth.exception.ForbiddenException;
import com.smarttask.auth.exception.ResourceNotFoundException;
import com.smarttask.auth.repository.UserRepository;
import com.smarttask.auth.service.UserService;
import com.smarttask.auth.utility.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ─── Create ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse createUser(
            RegisterUserRequest request,
            String callerRole) {

        requireAdmin(callerRole);

        if (userRepository.existsByUsername(
                request.getUsername())) {
            throw new ConflictException(
                    "Username '" + request.getUsername()
                    + "' is already taken");
        }

        if (userRepository.existsByEmail(
                request.getEmail())) {
            throw new ConflictException(
                    "Email '" + request.getEmail()
                    + "' is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(
                request.getPassword()));
        user.setRole(request.getRole() != null
                ? request.getRole()
                : Role.ROLE_USER);
        user.setActive(true);

        User saved = userRepository.save(user);
        log.info("User '{}' created by admin",
                saved.getUsername());

        return userMapper.toResponse(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", username));
        return toResponse(user);
    }

    // ─── Update ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse updateUser(Long id,
                                    UpdateUserRequest request) {
        User user = findOrThrow(id);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        // Only update email if it changed
        if (request.getEmail() != null
                && !request.getEmail().equals(user.getEmail())) {

            // Check new email not already used by someone else
            if (userRepository.existsByEmail(
                    request.getEmail())) {
                throw new ConflictException(
                        "Email '" + request.getEmail()
                        + "' is already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        User saved = userRepository.save(user);
        log.info("User '{}' updated", saved.getUsername());
        return toResponse(saved);
    }

    // ─── Change password ──────────────────────────────────────────────────

    @Override
    @Transactional
    public void changePassword(Long id,
                                ChangePasswordRequest request) {
        User user = findOrThrow(id);

        // Verify current password is correct
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {
            throw new BadRequestException(
                    "Current password is incorrect");
        }

        // New password must differ from current
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {
            throw new BadRequestException(
                    "New password must be different "
                    + "from current password");
        }

        // Confirm passwords must match
        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new BadRequestException(
                    "New password and confirm password "
                    + "do not match");
        }

        user.setPassword(passwordEncoder.encode(
                request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user '{}'",
                user.getUsername());
    }

    // ─── Enable / Disable ─────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse toggleActive(Long id, boolean active) {
        User user = findOrThrow(id);

        // Prevent disabling a user who is already disabled
        if (user.isActive() == active) {
            throw new BadRequestException(
                    "User is already "
                    + (active ? "active" : "inactive"));
        }

        user.setActive(active);
        User saved = userRepository.save(user);

        log.info("User '{}' set to {}",
                saved.getUsername(),
                active ? "active" : "inactive");
        return toResponse(saved);
    }

    // ─── Delete ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findOrThrow(id);
        userRepository.delete(user);
        log.info("User '{}' deleted", user.getUsername());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", id));
    }

    private UserResponse toResponse(User user) {
        DateTimeFormatter fmt =
                DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt() != null
                        ? user.getCreatedAt().format(fmt) : null)
                .updatedAt(user.getUpdatedAt() != null
                        ? user.getUpdatedAt().format(fmt) : null)
                .build();
    }
    private void requireAdmin(String callerRole) {
        if (!"ROLE_ADMIN".equals(callerRole)) {
            throw new ForbiddenException(
                    "Only ADMIN can perform this action");
        }
    }
}