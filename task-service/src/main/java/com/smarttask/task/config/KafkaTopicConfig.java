package com.smarttask.task.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_TASK_EVENTS = "task-events";

    @Bean
    public NewTopic taskEventsTopic() {
        return TopicBuilder.name(TOPIC_TASK_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}