package com.carrepair.backend.dto.response.analytics;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponseDTO {
    private AnalyticsStatsDTO stats;
    private List<TrendPointDTO> trend;
    private StatusDistributionDTO statusDistribution;
    private List<TopShopDTO> topShops;
}
