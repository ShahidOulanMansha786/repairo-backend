package com.carrepair.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String role;
}
