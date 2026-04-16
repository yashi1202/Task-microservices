package com.smarttask.gateway.messaging;

import com.smarttask.gateway.security.RevocationStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutEventListener {

    private final RevocationStore revocationStore;

    @Value("${app.kafka.topics.logout:auth.logout.v1}")
    private String logoutTopic;

    @KafkaListener(
            topics = "${app.kafka.topics.logout:auth.logout.v1}",
            groupId = "${spring.kafka.consumer.group-id:api-gateway}",
            containerFactory = "logoutEventKafkaListenerContainerFactory")
    public void onLogout(LogoutEvent event) {
        long expiresAt = event.getTokenExpiresAtEpochMs();
        revocationStore.revokeToken(event.getTokenId(), expiresAt);
        revocationStore.revokeSession(event.getSessionId(), expiresAt);
        log.info("Consumed logout event from topic={} tokenId={} sessionId={} username={}",
                logoutTopic, event.getTokenId(), event.getSessionId(), event.getUsername());
    }
}
