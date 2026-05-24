package com.carrepair.backend.dto.request.repairshop;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDocumentUploadDto {
    private String logoKey;
    private String cnicKey;
    private String businessDocKey;
}
