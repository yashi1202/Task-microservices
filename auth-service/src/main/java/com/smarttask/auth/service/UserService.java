package com.smarttask.auth.service;

import com.smarttask.auth.dto.ChangePasswordRequest;
import com.smarttask.auth.dto.ChangePasswordResponse;
import com.smarttask.auth.dto.RegisterUserRequest;
import com.smarttask.auth.dto.UpdateUserRequest;
import com.smarttask.auth.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(RegisterUserRequest request, String role);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse getUserByUsername(String username);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    //Page<UserResponse> findAll(Pageable pageable);
    UserResponse toggleActive(Long id, boolean active);
    void deleteUser(Long id);
    void changePassword(Long id, ChangePasswordRequest request);
	//void changePassword(Long id, ChangePasswordRequest request);
}
