package com.carrepair.backend.dto.response.repairshop;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopDetailDto {
    private Long shopId;
    private String shopName;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String address;
    private Double latitude;
    private Double longitude;
    private String approvalStatus;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private String logoUrl;
    private String cnicUrl;
    private String businessDocUrl;
}
