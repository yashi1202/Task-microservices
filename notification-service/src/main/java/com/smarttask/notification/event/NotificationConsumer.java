package com.smarttask.notification.event;

import com.smarttask.notification.service
        .NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = "task-events",
        groupId = "notification-group"
    )
    public void onTaskEvent(TaskEvent event) {
        if (event == null
                || event.getEventType() == null) {
            log.warn("Received null or invalid event");
            return;
        }

        log.info("Notification received: type={} taskId={}",
                event.getEventType(), event.getTaskId());

        try {
            notificationService.processEvent(event);
        } catch (Exception ex) {
            log.error("Failed to process notification "
                    + "for event type={} taskId={}: {}",
                    event.getEventType(),
                    event.getTaskId(),
                    ex.getMessage());
        }
    }
}