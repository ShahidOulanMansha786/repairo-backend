package com.carrepair.backend.dto.request.repairshop;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopLoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String role;
    private String approvalStatus;
    private String rejectionReason;
}
