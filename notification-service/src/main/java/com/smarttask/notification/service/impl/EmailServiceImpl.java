package com.smarttask.notification.service.impl;

import com.smarttask.notification.dto.NotificationRequest;
import com.smarttask.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notification.from}")
    private String from;

    @Value("${app.notification.enabled:false}")
    private boolean enabled;

    // ─── Send raw email ───────────────────────────────────────

    @Override
    public void sendEmail(NotificationRequest request) {
        if (!enabled) {
            log.info("[EMAIL DISABLED] Would send to: {} | "
                    + "Subject: {}",
                    request.getTo(), request.getSubject());
            log.debug("[EMAIL BODY]\n{}",
                    request.getBody());
            return;
        }

        if (request.getTo() == null
                || request.getTo().isBlank()) {
            log.warn("Cannot send email — recipient is empty");
            return;
        }

        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            mailSender.send(message);

            log.info("Email sent to '{}' — subject: '{}'",
                    request.getTo(), request.getSubject());

        } catch (Exception ex) {
            log.error("Failed to send email to '{}': {}",
                    request.getTo(), ex.getMessage());
        }
    }

    // ─── Task created ─────────────────────────────────────────

    @Override
    public void notifyTaskCreated(String to,
                                   String taskTitle,
                                   String createdBy) {
        sendEmail(NotificationRequest.builder()
                .to(to)
                .subject("[SmartTask] New task created: "
                        + taskTitle)
                .body(buildBody(
                        "A new task has been created.",
                        "Task     : " + taskTitle,
                        "Created by : " + createdBy,
                        null, null))
                .build());
    }

    // ─── Task assigned ────────────────────────────────────────

    @Override
    public void notifyTaskAssigned(String to,
                                    String taskTitle,
                                    String assignedBy) {
        sendEmail(NotificationRequest.builder()
                .to(to)
                .subject("[SmartTask] Task assigned to you: "
                        + taskTitle)
                .body(buildBody(
                        "A task has been assigned to you.",
                        "Task      : " + taskTitle,
                        "Assigned by : " + assignedBy,
                        null, null))
                .build());
    }

    // ─── Status changed ───────────────────────────────────────

    @Override
    public void notifyStatusChanged(String to,
                                     String taskTitle,
                                     String oldStatus,
                                     String newStatus,
                                     String changedBy) {
        sendEmail(NotificationRequest.builder()
                .to(to)
                .subject("[SmartTask] Task status updated: "
                        + taskTitle)
                .body(buildBody(
                        "A task status has been updated.",
                        "Task      : " + taskTitle,
                        "Changed by  : " + changedBy,
                        "Old status  : " + oldStatus,
                        "New status  : " + newStatus))
                .build());
    }

    // ─── Task deleted ─────────────────────────────────────────

    @Override
    public void notifyTaskDeleted(String to,
                                   String taskTitle,
                                   String deletedBy) {
        sendEmail(NotificationRequest.builder()
                .to(to)
                .subject("[SmartTask] Task deleted: "
                        + taskTitle)
                .body(buildBody(
                        "A task has been deleted.",
                        "Task      : " + taskTitle,
                        "Deleted by  : " + deletedBy,
                        null, null))
                .build());
    }

    // ─── Email body builder ───────────────────────────────────

    private String buildBody(String intro,
                              String line1,
                              String line2,
                              String line3,
                              String line4) {
        StringBuilder sb = new StringBuilder();
        sb.append("SmartTask Notification\n");
        sb.append("======================\n\n");
        sb.append(intro).append("\n\n");
        sb.append(line1).append("\n");
        sb.append(line2).append("\n");
        if (line3 != null) sb.append(line3).append("\n");
        if (line4 != null) sb.append(line4).append("\n");
        sb.append("\n-- SmartTask Platform\n");
        sb.append("Do not reply to this email.\n");
        return sb.toString();
    }
}