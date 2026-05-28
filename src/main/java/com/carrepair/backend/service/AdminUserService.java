package com.carrepair.backend.service;

import com.carrepair.backend.dto.ActivityLogDto;
import com.carrepair.backend.dto.UserCountsDto;
import com.carrepair.backend.dto.UserDetailDto;
import com.carrepair.backend.dto.UserListDto;
import com.carrepair.backend.entity.RepairShop;
import com.carrepair.backend.entity.Role;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.enums.ActivityType;
import com.carrepair.backend.repository.*;
import com.carrepair.backend.service.fcm.FcmService;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RepairShopRepository repairShopRepository;
    private final LeadRepository leadRepository;
    private final QuoteRepository quoteRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogService activityLogService;
    private final FcmService fcmService;

    public Page<UserListDto> getUsers(String role, String status, String search, Pageable pageable) {
        return userRepository.findAllWithFilters(role, status, search, pageable)
                .map(this::toUserListDto);
    }

    public UserCountsDto getCounts() {
        return UserCountsDto.builder()
                .all(userRepository.count())
                .carOwners(userRepository.countByRole(Role.CAR_OWNER))
                .shopOwners(userRepository.countByRole(Role.SHOP_OWNER))
                .admins(userRepository.countByRole(Role.ADMIN))
                .build();
    }

    public UserDetailDto getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long statsCount = null;
        String shopAddress = null;
        Double latitude = null;
        Double longitude = null;

        if (user.getRole() == Role.CAR_OWNER) {
            statsCount = leadRepository.countByCarOwnerId(id);
        } else if (user.getRole() == Role.SHOP_OWNER) {
            statsCount = quoteRepository.countByRepairShopUserId(id);
            repairShopRepository.findByUserId(id).ifPresent(shop -> {
            });
            RepairShop shop = repairShopRepository.findByUserId(id).orElse(null);
            if (shop != null) {
                shopAddress = shop.getAddress();
                if (shop.getLocation() != null) {
                    latitude = shop.getLocation().getY();
                    longitude = shop.getLocation().getX();
                }
            }
        }

        List<ActivityLogDto> activities = activityLogRepository
                .findTop10ByUserIdOrderByCreatedAtDesc(id)
                .stream()
                .map(a -> ActivityLogDto.builder()
                        .activityType(a.getActivityType().name())
                        .description(a.getDescription())
                        .entityId(a.getEntityId())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();

        return UserDetailDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .isBlocked(user.isBlocked())
                .createdAt(user.getCreatedAt())
                .statsCount(statsCount)
                .shopAddress(shopAddress)
                .latitude(latitude)
                .longitude(longitude)
                .recentActivity(activities)
                .build();
    }

    @Transactional
    public void blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBlocked(true);
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(id);

        if (user.getFcmToken() != null) {
            fcmService.sendAccountBlockedNotification(user.getFcmToken());
        }

        activityLogService.log(id, ActivityType.ACCOUNT_BLOCKED,
                "Account blocked by admin", null);
    }

    @Transactional
    public void unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBlocked(false);
        userRepository.save(user);

        if (user.getFcmToken() != null) {
            fcmService.sendAccountUnblockedNotification(user.getFcmToken());
        }

        activityLogService.log(id, ActivityType.ACCOUNT_UNBLOCKED,
                "Account unblocked by admin", null);
    }

    private UserListDto toUserListDto(User user) {
        return UserListDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .isBlocked(user.isBlocked())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
