package com.carrepair.backend.dto.response.analytics;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsStatsDTO {
    private long totalLeads;
    private double totalLeadsChange;

    private long activeShops;
    private double activeShopsChange;

    private long totalQuotes;
    private double totalQuotesChange;

    private double totalGMV;
    private double gmvChange;
}
