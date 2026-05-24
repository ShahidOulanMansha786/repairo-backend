package com.carrepair.backend.dto.response.quote;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponseDto {
    private Long id;
    private Long leadId;
    private Long repairShopId;
    private String shopName;
    private String shopLogoUrl;
    private BigDecimal price;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private String channelId;
}