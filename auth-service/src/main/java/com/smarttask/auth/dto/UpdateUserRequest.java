package com.smarttask.auth.dto;

import com.smarttask.auth.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
public class UpdateUserRequest {
	@Size(max = 100)
    private String fullName;

    @Email
    private String email;

    private Role role;

    private Boolean active;

}
