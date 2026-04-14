package com.smarttask.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssignTaskRequest {

    @NotBlank(message = "Username is required")
    private String username;

    private String fullName;
}