package com.carrepair.backend.service.dashboard;

import com.carrepair.backend.dto.response.dashboard.DashboardStatsResponse;
import com.carrepair.backend.dto.response.dashboard.RecentShopResponse;
import com.carrepair.backend.entity.Role;
import com.carrepair.backend.repository.ActivityLogRepository;
import com.carrepair.backend.repository.RepairShopRepository;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.ApprovalStatus;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RepairShopRepository shopRepo;
    private final UserRepository userRepo;
    private final ActivityLogRepository activityRepo;

    public DashboardStatsResponse getStats() {
        return DashboardStatsResponse.builder()
                .totalShops(shopRepo.count())
                .pendingVerifications(
                        shopRepo.countByApprovalStatus(
                                ApprovalStatus.PENDING))
                .carOwners(
                        userRepo.countByRole(Role.CAR_OWNER))
                .monthlyRevenue(45200.00) // hardcoded abhi
                .build();
    }

    public List<RecentShopResponse> getRecentShops() {
        return shopRepo
                .findTop5ByApprovalStatusOrderByCreatedAtDesc(
                        ApprovalStatus.PENDING)
                .stream()
                .map(shop -> RecentShopResponse.builder()
                        .id(shop.getId())
                        .shopName(shop.getShopName())
                        .address(shop.getAddress())
                        .approvalStatus(
                                shop.getApprovalStatus().name())
                        .createdAt(shop.getCreatedAt())
                        .build())
                .toList();
    }
}
