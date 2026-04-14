package com.smarttask.auth.service;

import com.smarttask.auth.dto.LoginRequest;
import com.smarttask.auth.dto.RegisterUserRequest;
import com.smarttask.auth.dto.UserResponse;
import com.smarttask.auth.dto.JwtAuthResponse;
 
public interface AuthService {
    JwtAuthResponse login(LoginRequest request, String clientIp, String userAgent);
    void logout(String token);
    UserResponse register(RegisterUserRequest user);
}
 
