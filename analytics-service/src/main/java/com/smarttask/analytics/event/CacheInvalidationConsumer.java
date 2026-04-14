package com.smarttask.analytics.event;

import com.smarttask.analytics.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Listens to task-events topic.
 * Evicts Redis dashboard cache whenever a task changes
 * so the next request always gets fresh data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheInvalidationConsumer {

    private final DashboardService dashboardService;

    @KafkaListener(
        topics = "task-events",
        groupId = "analytics-group"
    )
    public void onTaskEvent(TaskEvent event) {
        if (event == null
                || event.getEventType() == null) return;

        log.info("Analytics received: type={} taskId={}",
                event.getEventType(), event.getTaskId());

        // Any task change invalidates the dashboard cache
        // Next GET /api/dashboard will fetch fresh data
        dashboardService.evictDashboardCache();

        log.debug("Dashboard cache evicted due to: {}",
                event.getEventType());
    }
}