package com.smarttask.collab.service;

import com.smarttask.collab.dto.AddCommentRequest;
import com.smarttask.collab.dto.AuditLogResponse;
import com.smarttask.collab.dto.CommentResponse;
import com.smarttask.collab.event.TaskEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CollaborationService {

    // Comments
    CommentResponse addComment(Long taskId,
                                AddCommentRequest request,
                                String username,
                                String fullName);

    Page<CommentResponse> getCommentsByTask(Long taskId,
                                             Pageable pageable);

    Page<CommentResponse> getCommentsByUser(String username,
                                             Pageable pageable);

    void deleteComment(Long commentId,
                        String callerUsername,
                        String callerRole);

    // Audit logs
    void recordAuditLog(TaskEvent event);

    Page<AuditLogResponse> getAuditLogsByTask(Long taskId,
                                               Pageable pageable);

    Page<AuditLogResponse> getAuditLogsByUser(String username,
                                               Pageable pageable);
}