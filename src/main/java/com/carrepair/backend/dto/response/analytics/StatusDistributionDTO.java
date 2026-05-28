package com.carrepair.backend.dto.response.analytics;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusDistributionDTO {
    private double completed;
    private double cancelled;
    private double other;
    private long total;
}
