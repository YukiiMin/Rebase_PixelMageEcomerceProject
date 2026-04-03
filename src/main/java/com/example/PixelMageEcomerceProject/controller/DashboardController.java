package com.example.PixelMageEcomerceProject.controller;

import com.example.PixelMageEcomerceProject.dto.response.DashboardResponse;
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
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Dashboard Statistics API")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Get Dashboard Statistics", description = "Retrieves total users, orders, revenue, grouped revenue, and recent orders.")
    public ResponseEntity<ResponseBase<DashboardResponse>> getDashboardStats() {
        return ResponseBase.ok(dashboardService.getDashboardStats(), "Dashboard stats retrieved successfully");
    }
}
