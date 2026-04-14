package com.smarttask.notification.service;

import com.smarttask.notification.dto.NotificationRequest;

public interface EmailService {

    void sendEmail(NotificationRequest request);

    void notifyTaskCreated(String to, String taskTitle,
                            String createdBy);

    void notifyTaskAssigned(String to, String taskTitle,
                             String assignedBy);

    void notifyStatusChanged(String to, String taskTitle,
                              String oldStatus,
                              String newStatus,
                              String changedBy);

    void notifyTaskDeleted(String to, String taskTitle,
                            String deletedBy);
}