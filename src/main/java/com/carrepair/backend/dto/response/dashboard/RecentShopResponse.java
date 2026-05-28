package com.carrepair.backend.dto.response.dashboard;

import lombok.*;

import java.time.LocalDateTime;

@Builder @Getter
public class RecentShopResponse {
    private Long id;
    private String shopName;
    private String address;
    private String approvalStatus;
    private LocalDateTime createdAt;
}
