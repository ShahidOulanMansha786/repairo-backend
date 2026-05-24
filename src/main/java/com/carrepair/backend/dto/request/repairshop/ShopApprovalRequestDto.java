package com.carrepair.backend.dto.request.repairshop;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopApprovalRequestDto {
    private String rejectionReason;
}
