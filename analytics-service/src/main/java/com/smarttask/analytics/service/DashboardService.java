package com.smarttask.analytics.service;

import com.smarttask.analytics.dto.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard();

    void evictDashboardCache();
}