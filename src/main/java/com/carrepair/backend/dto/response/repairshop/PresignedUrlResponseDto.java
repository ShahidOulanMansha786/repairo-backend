package com.carrepair.backend.dto.response.repairshop;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponseDto {
    private String uploadUrl;
    private String objectKey;
}
