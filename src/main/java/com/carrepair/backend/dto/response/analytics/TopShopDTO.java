package com.carrepair.backend.dto.response.analytics;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopShopDTO {
    private Long shopId;
    private String shopName;
    private String location;
    private long bookings;
    private double gmv;        // hardcoded
    private double rating;     // hardcoded
    private String status;
}
