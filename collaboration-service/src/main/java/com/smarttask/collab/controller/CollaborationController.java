package com.smarttask.collab.controller;

import com.smarttask.collab.dto.AddCommentRequest;
import com.smarttask.collab.dto.ApiResponse;
import com.smarttask.collab.dto.AuditLogResponse;
import com.smarttask.collab.dto.CommentResponse;
import com.smarttask.collab.exception.ForbiddenException;
import com.smarttask.collab.service.CollaborationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService collaborationService;

    // ─── Add comment ──────────────────────────────────────────────────────

    @PostMapping("/api/comments/tasks/{taskId}")
    public ResponseEntity<ApiResponse<CommentResponse>>
            addComment(
                    @PathVariable Long taskId,
                    @Valid @RequestBody AddCommentRequest req,
                    @RequestHeader("X-Auth-Username")
                            String username,
                    @RequestHeader(value = "X-Auth-FullName",
                            required = false)
                            String fullName) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Comment added successfully",
                        collaborationService.addComment(
                                taskId, req,
                                username, fullName)));
    }

    // ─── Get comments for task ────────────────────────────────────────────

    @GetMapping("/api/comments/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>>
            getCommentsByTask(
                    @PathVariable Long taskId,
                    @RequestParam(defaultValue = "0")  int page,
                    @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                collaborationService.getCommentsByTask(
                        taskId, pageable)));
    }

    // ─── Get my comments ──────────────────────────────────────────────────

    @GetMapping("/api/comments/my-comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>>
            getMyComments(
                    @RequestHeader("X-Auth-Username")
                            String username,
                    @RequestParam(defaultValue = "0")  int page,
                    @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                collaborationService.getCommentsByUser(
                        username, pageable)));
    }

    // ─── Delete comment ───────────────────────────────────────────────────

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("X-Auth-Username") String username,
            @RequestHeader("X-Auth-Role")     String role) {

        collaborationService.deleteComment(
                commentId, username, role);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comment deleted successfully", null));
    }

    // ─── Get audit logs for task ──────────────────────────────────────────

    @GetMapping("/api/audit/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>>
            getAuditByTask(
                    @PathVariable Long taskId,
                    @RequestParam(defaultValue = "0")  int page,
                    @RequestParam(defaultValue = "20") int size,
                    @RequestHeader("X-Auth-Role")
                            String role) {

        // Only ADMIN and MANAGER can view audit logs
        if (!"ROLE_ADMIN".equals(role)
                && !"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(
                    "Only ADMIN or MANAGER can "
                    + "view audit logs");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                collaborationService.getAuditLogsByTask(
                        taskId, pageable)));
    }

    // ─── Get audit logs by user ───────────────────────────────────────────

    @GetMapping("/api/audit/users/{username}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>>
            getAuditByUser(
                    @PathVariable String username,
                    @RequestParam(defaultValue = "0")  int page,
                    @RequestParam(defaultValue = "20") int size,
                    @RequestHeader("X-Auth-Role")
                            String role) {

        // Only ADMIN can view audit logs by user
        if (!"ROLE_ADMIN".equals(role)) {
            throw new ForbiddenException(
                    "Only ADMIN can view user audit logs");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                collaborationService.getAuditLogsByUser(
                        username, pageable)));
    }
}