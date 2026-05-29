package com.carrepair.backend.dto.response.dispute;

import com.carrepair.backend.entity.Dispute;
import com.carrepair.backend.entity.Lead;
import com.carrepair.backend.entity.RepairShop;
import com.carrepair.backend.entity.User;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeDetailDTO {
    private Dispute dispute;
    private Lead lead;
    private User carOwner;
    private RepairShop repairShop;
    private User shopUser;
}
