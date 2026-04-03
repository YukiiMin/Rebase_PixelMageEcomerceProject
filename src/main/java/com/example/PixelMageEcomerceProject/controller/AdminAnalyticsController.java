package com.example.PixelMageEcomerceProject.controller;

import com.example.PixelMageEcomerceProject.dto.response.AnalyticsResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.service.interfaces.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Admin Analytics", description = "Analytics Statistics API")
@SecurityRequirement(name = "bearerAuth")
public class AdminAnalyticsController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Get Analytics Statistics", description = "Retrieves statistics for sessions, spread usage, registrations, and rarity.")
    public ResponseEntity<ResponseBase<AnalyticsResponse>> getAnalyticsStats() {
        return ResponseBase.ok(dashboardService.getAnalytics(), "Analytics stats retrieved successfully");
    }
}
