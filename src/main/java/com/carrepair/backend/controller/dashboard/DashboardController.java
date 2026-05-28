package com.carrepair.backend.controller.dashboard;

import com.carrepair.backend.dto.response.dashboard.DashboardStatsResponse;
import com.carrepair.backend.dto.response.dashboard.RecentShopResponse;
import com.carrepair.backend.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(
                dashboardService.getStats());
    }

    @GetMapping("/recent-shops")
    public ResponseEntity<List<RecentShopResponse>>
    getRecentShops() {
        return ResponseEntity.ok(
                dashboardService.getRecentShops());
    }
}
