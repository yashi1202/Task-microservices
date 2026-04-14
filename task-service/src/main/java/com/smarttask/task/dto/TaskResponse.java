package com.smarttask.task.dto;

import com.smarttask.task.enums.Priority;
import com.smarttask.task.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long       id;
    private String     title;
    private String     description;
    private Priority   priority;
    private TaskStatus status;
    private LocalDate  dueDate;
    private boolean    overdue;

    // Assignment info
    private String     assignedToUsername;
    private String     assignedToFullName;

    // Audit info
    private String     createdBy;
    private String     lastModifiedBy;
    private String     createdAt;
    private String     updatedAt;
    private Long       version;
}