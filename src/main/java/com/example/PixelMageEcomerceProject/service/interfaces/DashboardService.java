package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.response.DashboardResponse;
import com.example.PixelMageEcomerceProject.dto.response.AnalyticsResponse;

public interface DashboardService {
    DashboardResponse getDashboardStats();
    AnalyticsResponse getAnalytics();
}
