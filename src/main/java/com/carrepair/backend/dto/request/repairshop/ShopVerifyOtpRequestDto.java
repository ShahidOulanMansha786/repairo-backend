package com.carrepair.backend.dto.request.repairshop;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ShopVerifyOtpRequestDto {
    private String email;
    private String otp;
}