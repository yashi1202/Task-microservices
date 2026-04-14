package com.smarttask.task.event;

import com.smarttask.task.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventPublisher {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    public void publish(TaskEvent event) {
        // Partition by taskId so events for same task
        // always go to same partition — preserving order
        String key = String.valueOf(event.getTaskId());

        kafkaTemplate.send(
                KafkaTopicConfig.TOPIC_TASK_EVENTS, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event " +
                                "type={} taskId={}: {}",
                                event.getEventType(),
                                event.getTaskId(),
                                ex.getMessage());
                    } else {
                        log.debug("Published event type={} " +
                                "taskId={} partition={}",
                                event.getEventType(),
                                event.getTaskId(),
                                result.getRecordMetadata()
                                        .partition());
                    }
                });
    }
}