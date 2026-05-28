package com.carrepair.backend.dto.response.analytics;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointDTO {
    private String date;
    private long count;
}
