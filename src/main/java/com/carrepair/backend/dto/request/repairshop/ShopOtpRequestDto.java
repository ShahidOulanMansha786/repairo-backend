package com.carrepair.backend.dto.request.repairshop;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ShopOtpRequestDto {
    private String fullName;
    private String email;
    private String phone;
    private String shopName;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
}
