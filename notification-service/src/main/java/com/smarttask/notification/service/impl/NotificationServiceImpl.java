package com.smarttask.notification.service.impl;

import com.smarttask.notification.dto.NotificationRequest;
import com.smarttask.notification.event.TaskEvent;
import com.smarttask.notification.service.EmailService;
import com.smarttask.notification.service
        .NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final EmailService emailService;

    @Override
    public void processEvent(TaskEvent event) {
        if (event == null
                || event.getEventType() == null) return;

        log.info("Processing notification for: "
                + "type={} taskId={}",
                event.getEventType(), event.getTaskId());

        switch (event.getEventType()) {

            case TASK_CREATED:
                handleTaskCreated(event);
                break;

            case TASK_ASSIGNED:
                handleTaskAssigned(event);
                break;

            case TASK_STATUS_CHANGED:
                handleStatusChanged(event);
                break;
            case TASK_APPROVED:
                handleTaskApproved(event);  // ← add this
                break;

            case TASK_DELETED:
                handleTaskDeleted(event);
                break;

            case TASK_UPDATED:
            case TASK_UNASSIGNED:
                // Suppress noisy notifications
                // for minor updates
                log.debug("Suppressing notification "
                        + "for event type: {}",
                        event.getEventType());
                break;
        }
    }

    // ─── Handlers ─────────────────────────────────────────────

    private void handleTaskCreated(TaskEvent event) {
        // Notify the person who created the task
        String recipient = resolveEmail(
                event.getPerformedBy());
        emailService.notifyTaskCreated(
                recipient,
                event.getTitle(),
                event.getPerformedBy());
    }

    private void handleTaskAssigned(TaskEvent event) {
        // Notify the user who was assigned the task
        if (event.getAssignedToUsername() == null
                && event.getNewAssignee() == null) {
            log.warn("No assignee found for "
                    + "TASK_ASSIGNED event taskId={}",
                    event.getTaskId());
            return;
        }

        String assignee = event.getAssignedToUsername()
                != null
                ? event.getAssignedToUsername()
                : event.getNewAssignee();

        String recipient = resolveEmail(assignee);
        emailService.notifyTaskAssigned(
                recipient,
                event.getTitle(),
                event.getPerformedBy());
    }

    private void handleStatusChanged(TaskEvent event) {
        // Notify the assigned user about status change
        String assignee = event.getAssignedToUsername();
        if (assignee == null) {
            log.debug("No assignee for status "
                    + "change notification taskId={}",
                    event.getTaskId());
            return;
        }

        String recipient = resolveEmail(assignee);
        emailService.notifyStatusChanged(
                recipient,
                event.getTitle(),
                event.getOldStatus(),
                event.getNewStatus(),
                event.getPerformedBy());
    }
    private void handleTaskApproved(TaskEvent event) {
        String assignee = event.getAssignedToUsername();
        if (assignee == null || assignee.isBlank()) {
            log.warn("No assignee for TASK_APPROVED "
                    + "taskId={}", event.getTaskId());
            return;
        }
        emailService.sendEmail(
                NotificationRequest.builder()
                        .to(resolveEmail(assignee))
                        .subject("[SmartTask] Task approved: "
                                + event.getTitle())
                        .body(buildApprovalBody(event))
                        .build());

        log.info("Approval notification sent to '{}' "
                + "for task #{}", assignee,
                event.getTaskId());
    }
    
    private String buildApprovalBody(TaskEvent event) {
        return "SmartTask Notification\n"
                + "======================\n\n"
                + "Your task has been reviewed "
                + "and approved.\n\n"
                + "Task     : " + event.getTitle() + "\n"
                + "Approved by : "
                + event.getPerformedBy() + "\n"
                + "Status   : APPROVED\n"
                + "At       : " + event.getOccurredAt()
                + "\n\n"
                + "-- SmartTask Platform\n"
                + "Do not reply to this email.\n";
    }
    

    private void handleTaskDeleted(TaskEvent event) {
        // Notify the person who deleted the task
        String recipient = resolveEmail(
                event.getPerformedBy());
        emailService.notifyTaskDeleted(
                recipient,
                event.getTitle(),
                event.getPerformedBy());
    }

    // ─── Helper ───────────────────────────────────────────────

    /**
     * Resolves email address from username.
     * In production this would call auth-service
     * to get the real email.
     * For now we use a convention:
     * username@smarttask.com
     */
    private String resolveEmail(String username) {
        if (username == null || username.isBlank()) {
            return "noreply@smarttask.com";
        }
        return username + "@smarttask.com";
    }
}