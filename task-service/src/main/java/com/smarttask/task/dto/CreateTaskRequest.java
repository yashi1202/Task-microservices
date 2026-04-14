package com.smarttask.task.dto;

import com.smarttask.task.enums.Priority;
import com.smarttask.task.enums.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000)
    private String description;

    private Priority priority = Priority.MEDIUM;

    private TaskStatus status = TaskStatus.OPEN;

    @FutureOrPresent(message = "Due date must be today or future")
    private LocalDate dueDate;

    // Username of the user to assign the task to
    // Only ADMIN and MANAGER can set this
    private String assignedToUsername;
}