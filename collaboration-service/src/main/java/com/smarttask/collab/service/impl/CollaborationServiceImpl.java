package com.smarttask.collab.service.impl;

import com.smarttask.collab.dto.AddCommentRequest;
import com.smarttask.collab.dto.AuditLogResponse;
import com.smarttask.collab.dto.CommentResponse;
import com.smarttask.collab.entity.AuditLog;
import com.smarttask.collab.entity.Comment;
import com.smarttask.collab.event.TaskEvent;
import com.smarttask.collab.exception.BadRequestException;
import com.smarttask.collab.exception.ForbiddenException;
import com.smarttask.collab.exception.ResourceNotFoundException;
import com.smarttask.collab.repository.AuditLogRepository;
import com.smarttask.collab.repository.CommentRepository;
import com.smarttask.collab.service.CollaborationService;
import com.smarttask.collab.util.CollabMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborationServiceImpl
        implements CollaborationService {

    private final CommentRepository  commentRepository;
    private final AuditLogRepository auditLogRepository;
    private final CollabMapper       collabMapper;

    // ─── Add Comment ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public CommentResponse addComment(Long taskId,
                                       AddCommentRequest request,
                                       String username,
                                       String fullName) {

        if (taskId == null || taskId <= 0) {
            throw new BadRequestException(
                    "Invalid task ID: " + taskId);
        }

        Comment comment = Comment.builder()
                .taskId(taskId)
                .authorUsername(username)
                .authorFullName(fullName != null
                        ? fullName : username)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("Comment added to task #{} by '{}'",
                taskId, username);

        return collabMapper.toCommentResponse(saved);
    }

    // ─── Get Comments by Task ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByTask(
            Long taskId, Pageable pageable) {

        return commentRepository
                .findByTaskIdOrderByCreatedAtDesc(
                        taskId, pageable)
                .map(collabMapper::toCommentResponse);
    }

    // ─── Get Comments by User ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByUser(
            String username, Pageable pageable) {

        return commentRepository
                .findByAuthorUsernameOrderByCreatedAtDesc(
                        username, pageable)
                .map(collabMapper::toCommentResponse);
    }

    // ─── Delete Comment ───────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteComment(Long commentId,
                               String callerUsername,
                               String callerRole) {

        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comment", "id", commentId));

        // Only the author or ADMIN can delete a comment
        boolean isAuthor = comment.getAuthorUsername()
                .equals(callerUsername);
        boolean isAdmin  = "ROLE_ADMIN".equals(callerRole);

        if (!isAuthor && !isAdmin) {
            throw new ForbiddenException(
                    "You can only delete your own comments");
        }

        commentRepository.delete(comment);
        log.info("Comment #{} deleted by '{}'",
                commentId, callerUsername);
    }

    // ─── Record Audit Log (from Kafka) ────────────────────────────────────

    @Override
    @Transactional
    public void recordAuditLog(TaskEvent event) {
        if (event == null
                || event.getEventType() == null) return;

        String description = buildDescription(event);
        String oldValue    = resolveOldValue(event);
        String newValue    = resolveNewValue(event);

        AuditLog entry = AuditLog.builder()
                .taskId(event.getTaskId())
                .taskTitle(event.getTitle())
                .action(event.getEventType().name())
                .performedBy(event.getPerformedBy())
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .build();

        auditLogRepository.save(entry);
        log.debug("Audit log saved: taskId={} action={}",
                event.getTaskId(), event.getEventType());
    }

    // ─── Get Audit Logs by Task ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByTask(
            Long taskId, Pageable pageable) {

        return auditLogRepository
                .findByTaskIdOrderByCreatedAtDesc(
                        taskId, pageable)
                .map(collabMapper::toAuditResponse);
    }

    // ─── Get Audit Logs by User ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByUser(
            String username, Pageable pageable) {

        return auditLogRepository
                .findByPerformedByOrderByCreatedAtDesc(
                        username, pageable)
                .map(collabMapper::toAuditResponse);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private String buildDescription(TaskEvent event) {
        return switch (event.getEventType()) {
            case TASK_CREATED ->
                "Task '" + event.getTitle()
                + "' created by " + event.getPerformedBy();
            case TASK_UPDATED ->
                "Task '" + event.getTitle()
                + "' updated by " + event.getPerformedBy();
            case TASK_STATUS_CHANGED ->
                "Status changed from ["
                + event.getOldStatus() + "] to ["
                + event.getNewStatus() + "] by "
                + event.getPerformedBy();
            case TASK_ASSIGNED ->
                "Task assigned to '"
                + event.getNewAssignee()
                + "' by " + event.getPerformedBy();
            case TASK_UNASSIGNED ->
                "Task unassigned from '"
                + event.getOldAssignee()
                + "' by " + event.getPerformedBy();
            case TASK_DELETED ->
                "Task '" + event.getTitle()
                + "' deleted by " + event.getPerformedBy();
            case TASK_APPROVED ->           // ← add this
            "Task '" + event.getTitle()
            + "' given final approval by admin '"
            + event.getPerformedBy() + "'";
        };
    }

    private String resolveOldValue(TaskEvent event) {
        return switch (event.getEventType()) {
            case TASK_STATUS_CHANGED -> event.getOldStatus();
            case TASK_ASSIGNED,
                 TASK_UNASSIGNED    -> event.getOldAssignee();
            default                 -> null;
        };
    }

    private String resolveNewValue(TaskEvent event) {
        return switch (event.getEventType()) {
            case TASK_CREATED        -> event.getTitle();
            case TASK_STATUS_CHANGED -> event.getNewStatus();
            case TASK_ASSIGNED       -> event.getNewAssignee();
            default                  -> null;
        };
    }
    
}