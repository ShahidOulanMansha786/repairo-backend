package com.carrepair.backend.dto.response.repairshop;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ShopAuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String role;
}
