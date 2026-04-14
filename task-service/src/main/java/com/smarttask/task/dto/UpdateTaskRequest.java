package com.smarttask.task.dto;

import com.smarttask.task.enums.Priority;
import com.smarttask.task.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UpdateTaskRequest {

    @Size(max = 255)
    private String title;

    @Size(max = 5000)
    private String description;

    private Priority priority;

    private TaskStatus status;

    private LocalDate dueDate;

    // Only ADMIN and MANAGER can change this
    private String assignedToUsername;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
}