package com.carrepair.backend.dto.response.repairshop;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopApprovalResponseDto {
    private Long shopId;
    private String approvalStatus;
    private String message;
}
