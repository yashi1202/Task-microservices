package com.smarttask.auth.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutEventPublisher {

    private final KafkaTemplate<String, LogoutEvent> kafkaTemplate;

    @Value("${app.kafka.topics.logout:auth.logout.v1}")
    private String logoutTopic;

    public void publish(LogoutEvent event) {
        kafkaTemplate.send(logoutTopic, event.getUsername(), event);
        log.info("Published logout event tokenId={} sessionId={} username={}",
                event.getTokenId(), event.getSessionId(), event.getUsername());
    }
}
