package com.carrepair.backend.dto.response.dashboard;

import lombok.*;

@Builder @Getter
public class DashboardStatsResponse {
    private long totalShops;
    private long pendingVerifications;
    private long carOwners;
    private double monthlyRevenue;
}