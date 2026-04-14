package com.smarttask.analytics.controller;

import com.smarttask.analytics.dto.DashboardResponse;
import com.smarttask.analytics.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<?> getDashboard(
            @RequestHeader("X-Auth-Role")
                    String callerRole) {

        if (!"ROLE_ADMIN".equals(callerRole)
                && !"ROLE_MANAGER".equals(callerRole)) {
            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                        "message",
                        "Only ADMIN or MANAGER "
                        + "can view dashboard"));
        }

        DashboardResponse dashboard =
                dashboardService.getDashboard();

        // Return 200 even when fallback
        // so UI can show the message gracefully
        return ResponseEntity.ok(
                Map.of(
                    "success", true,
                    "data", dashboard,
                    "message",
                    dashboard.isServiceAvailable()
                        ? "Dashboard loaded"
                        : dashboard.getFallbackMessage()
                ));
    }

    @DeleteMapping("/cache")
    public ResponseEntity<?> evictCache(
            @RequestHeader("X-Auth-Role")
                    String callerRole) {

        if (!"ROLE_ADMIN".equals(callerRole)) {
            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                        "message",
                        "Only ADMIN can evict cache"));
        }

        dashboardService.evictDashboardCache();
        return ResponseEntity.ok(
                Map.of(
                    "success", true,
                    "message",
                    "Cache evicted successfully"));
    }
}