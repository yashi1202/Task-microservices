package com.smarttask.analytics.service.impl;

import com.smarttask.analytics.dto.DashboardResponse;
import com.smarttask.analytics.service.DashboardService;
import io.github.resilience4j.circuitbreaker
        .annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation
        .CacheEvict;
import org.springframework.cache.annotation
        .Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function
        .client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl
        implements DashboardService {

    private final WebClient taskServiceClient;

    private static final String CB_NAME =
            "taskService";

    private static final String CACHE_NAME =
            "dashboard";
    @Autowired
    private ObjectMapper objectMapper;
    // ─── Get Dashboard ────────────────────────────

    @Override
    @Cacheable(
        value = CACHE_NAME,
        key = "'global'"
    )
    @CircuitBreaker(
        name = CB_NAME,
        fallbackMethod = "getDashboardFallback"
    )
    public DashboardResponse getDashboard() {
        log.info("Fetching dashboard from "
                + "task-service...");

        Map<String, Object> response =
                taskServiceClient.get()
                        .uri("/api/tasks"
                            + "?size=1000&page=0")
                        .retrieve()
                        .bodyToMono(
                            new ParameterizedTypeReference
                                <Map<String, Object>>() {})
                        .block();

        if (response == null) {
            throw new RuntimeException(
                    "task-service returned null");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data =
                (Map<String, Object>)
                response.getOrDefault(
                        "data", new HashMap<>());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks =
                (List<Map<String, Object>>)
                data.getOrDefault(
                        "content", List.of());

        log.info("Dashboard built from {} tasks",
                tasks.size());

        return buildDashboard(tasks, false);
    }

    // ─── Fallback — circuit is OPEN ───────────────
    // Called automatically when:
    // 1. Circuit is OPEN (too many failures)
    // 2. Call times out (> 3 seconds)
    // 3. task-service throws an exception

    public DashboardResponse getDashboardFallback(
            Throwable ex) {
        log.warn("Circuit breaker triggered — "
                + "returning fallback dashboard. "
                + "Reason: {}",
                ex.getMessage());

        return DashboardResponse.builder()
                .totalTasks(0)
                .openTasks(0)
                .inProgressTasks(0)
                .doneTasks(0)
                .approvedTasks(0)
                .overdueTasks(0)
                .unassignedTasks(0)
                .tasksByUser(Map.of())
                .tasksByPriority(Map.of())
                .tasksByStatus(Map.of())
                .generatedAt(
                        LocalDateTime.now().toString())
                .fromCache(false)
                .serviceAvailable(false)  // ← key flag
                .fallbackMessage(
                        "Dashboard data temporarily "
                        + "unavailable. "
                        + "task-service is down. "
                        + "Retrying in 30 seconds.")
                .build();
    }

    // ─── Evict Cache ──────────────────────────────

    @Override
    @CacheEvict(
        value = CACHE_NAME,
        allEntries = true
    )
    public void evictDashboardCache() {
        log.info("Dashboard cache evicted");
    }

    // ─── Build Dashboard ──────────────────────────

    private DashboardResponse buildDashboard(
            List<Map<String, Object>> tasks,
            boolean fromCache) {

        long open = count(tasks, "OPEN");
        long inProgress = count(tasks, "IN_PROGRESS");
        long done = count(tasks, "DONE");
        long approved = count(tasks, "APPROVED");
        long overdue = tasks.stream()
                .filter(t -> Boolean.TRUE
                        .equals(t.get("overdue")))
                .count();
        long unassigned = tasks.stream()
                .filter(t -> t.get(
                        "assignedToUsername") == null)
                .count();

        Map<String, Long> byUser = tasks.stream()
                .filter(t -> t.get(
                        "assignedToUsername") != null)
                .collect(Collectors.groupingBy(
                        t -> t.get("assignedToUsername")
                                .toString(),
                        Collectors.counting()));

        Map<String, Long> byPriority = tasks.stream()
                .filter(t -> t.get("priority") != null)
                .collect(Collectors.groupingBy(
                        t -> t.get("priority")
                                .toString(),
                        Collectors.counting()));

        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("OPEN", open);
        byStatus.put("IN_PROGRESS", inProgress);
        byStatus.put("DONE", done);
        byStatus.put("APPROVED", approved);

        return DashboardResponse.builder()
                .totalTasks(tasks.size())
                .openTasks(open)
                .inProgressTasks(inProgress)
                .doneTasks(done)
                .approvedTasks(approved)
                .overdueTasks(overdue)
                .unassignedTasks(unassigned)
                .tasksByUser(byUser)
                .tasksByPriority(byPriority)
                .tasksByStatus(byStatus)
                .generatedAt(
                        LocalDateTime.now().toString())
                .fromCache(fromCache)
                .serviceAvailable(true)
                .fallbackMessage(null)
                .build();
    }

    private long count(
            List<Map<String, Object>> tasks,
            String status) {
        return tasks.stream()
                .filter(t -> status.equals(
                        t.get("status")))
                .count();
    }
}