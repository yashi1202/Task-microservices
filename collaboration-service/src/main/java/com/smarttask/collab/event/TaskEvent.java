package com.smarttask.collab.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {

    public enum EventType {
        TASK_CREATED,
        TASK_UPDATED,
        TASK_STATUS_CHANGED,
        TASK_ASSIGNED,
        TASK_UNASSIGNED,
        TASK_DELETED,
        TASK_APPROVED
    }

    private EventType eventType;
    private Long      taskId;
    private String    title;
    private String    status;
    private String    priority;
    private String    assignedToUsername;
    private String    assignedToFullName;
    private String    performedBy;
    private String    oldStatus;
    private String    newStatus;
    private String    oldAssignee;
    private String    newAssignee;
    private LocalDate dueDate;
    private String    occurredAt;
}