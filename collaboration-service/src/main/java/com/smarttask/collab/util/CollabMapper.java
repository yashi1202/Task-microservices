package com.smarttask.collab.util;

import com.smarttask.collab.dto.AuditLogResponse;
import com.smarttask.collab.dto.CommentResponse;
import com.smarttask.collab.entity.AuditLog;
import com.smarttask.collab.entity.Comment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CollabMapper {

    public CommentResponse toCommentResponse(Comment c) {
        if (c == null) return null;
        return CommentResponse.builder()
                .id(c.getId())
                .taskId(c.getTaskId())
                .authorUsername(c.getAuthorUsername())
                .authorFullName(c.getAuthorFullName())
                .content(c.getContent())
                .createdAt(formatDate(c.getCreatedAt()))
                .build();
    }

    public AuditLogResponse toAuditResponse(AuditLog a) {
        if (a == null) return null;
        return AuditLogResponse.builder()
                .id(a.getId())
                .taskId(a.getTaskId())
                .taskTitle(a.getTaskTitle())
                .action(a.getAction())
                .performedBy(a.getPerformedBy())
                .oldValue(a.getOldValue())
                .newValue(a.getNewValue())
                .description(a.getDescription())
                .createdAt(formatDate(a.getCreatedAt()))
                .build();
    }

    private String formatDate(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}