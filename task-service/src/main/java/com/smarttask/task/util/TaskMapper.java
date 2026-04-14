package com.smarttask.task.util;

import com.smarttask.task.dto.TaskResponse;
import com.smarttask.task.entity.Task;
import com.smarttask.task.enums.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        if (task == null) return null;

        boolean overdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.DONE;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .overdue(overdue)
                .assignedToUsername(task.getAssignedToUsername())
                .assignedToFullName(task.getAssignedToFullName())
                .createdBy(task.getCreatedBy())
                .lastModifiedBy(task.getLastModifiedBy())
                .createdAt(formatDate(task.getCreatedAt()))
                .updatedAt(formatDate(task.getUpdatedAt()))
                .version(task.getVersion())
                .build();
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}