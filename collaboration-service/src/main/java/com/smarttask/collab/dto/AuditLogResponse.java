package com.smarttask.collab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long   id;
    private Long   taskId;
    private String taskTitle;
    private String action;
    private String performedBy;
    private String oldValue;
    private String newValue;
    private String description;
    private String createdAt;
}