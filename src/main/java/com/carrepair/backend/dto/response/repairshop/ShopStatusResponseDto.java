package com.carrepair.backend.dto.response.repairshop;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopStatusResponseDto {
    private String shopName;
    private String approvalStatus;
    private String rejectionReason;
}
