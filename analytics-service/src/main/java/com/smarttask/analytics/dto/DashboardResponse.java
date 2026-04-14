package com.smarttask.analytics.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Builder
public class DashboardResponse implements Serializable {

    private final int    totalTasks;
    private final long   openTasks;
    private final long   inProgressTasks;
    private final long   doneTasks;
    private final long   approvedTasks;
    private final long   overdueTasks;
    private final long   unassignedTasks;

    private final Map<String, Long> tasksByUser;
    private final Map<String, Long> tasksByPriority;
    private final Map<String, Long> tasksByStatus;

    private final String  generatedAt;
    private final boolean fromCache;

    // Circuit breaker fields
    private final boolean serviceAvailable;
    private final String  fallbackMessage;
}