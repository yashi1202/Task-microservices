package com.smarttask.auth.service;

import com.smarttask.auth.dto.JwtAuthResponse;
import com.smarttask.auth.dto.LoginRequest;
import com.smarttask.auth.dto.RegisterUserRequest;
import com.smarttask.auth.dto.UserResponse;
import com.smarttask.auth.model.SessionData;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AuthService {

    JwtAuthResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest);

    void logout(
            String token,
            String sessionId);

    // ← add this
    /*UserResponse register(
            RegisterUserRequest request,
            String callerRole);*/

    boolean isSessionValid(String sessionId);

    void refreshSession(String sessionId);

    List<SessionData> getUserSessions(
            String username);
}