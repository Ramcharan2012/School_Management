package com.school.management.dashboard.controller;

import com.school.management.common.response.ApiResponse;
import com.school.management.dashboard.service.DashboardService;
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
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "High-level school statistics for admin users")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get overall school statistics — cached for performance")
    public ResponseEntity<ApiResponse<DashboardService.DashboardStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved.",
                dashboardService.getAdminDashboardStats()));
    }
}
