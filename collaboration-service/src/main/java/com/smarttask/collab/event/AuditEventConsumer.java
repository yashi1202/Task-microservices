package com.smarttask.collab.event;

import com.smarttask.collab.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final CollaborationService collaborationService;

    @KafkaListener(
        topics = "task-events",
        groupId = "collaboration-group"
    )
    public void onTaskEvent(TaskEvent event) {
        if (event == null
                || event.getEventType() == null) return;

        log.info("Collab received: type={} taskId={}",
                event.getEventType(), event.getTaskId());

        try {
            collaborationService.recordAuditLog(event);
        } catch (Exception ex) {
            log.error("Failed to record audit log "
                    + "for event {}: {}",
                    event.getEventType(), ex.getMessage());
        }
    }
}