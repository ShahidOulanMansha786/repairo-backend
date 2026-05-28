package com.carrepair.backend.service.dashboard;

import com.carrepair.backend.dto.response.dashboard.ActivityLogResponse;
import com.carrepair.backend.entity.ActivityLog;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.enums.ActivityType;
import com.carrepair.backend.repository.ActivityLogRepository;
import com.carrepair.backend.repository.UserRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityLogRepository activityRepo;
    private final UserRepository userRepo;

    public List<ActivityLogResponse> getRecentActivity() {
        return activityRepo
                .findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<ActivityLogResponse> getAllActivity(
            String type, Pageable pageable) {

        Page<ActivityLog> logs;

        if (type != null && !type.isBlank()) {
            log.info("filter type: {type}");
            logs = activityRepo
                    .findByActivityTypeOrderByCreatedAtDesc(
                            ActivityType.valueOf(type), pageable);
        } else {
            logs = activityRepo
                    .findAllByOrderByCreatedAtDesc(pageable);
        }

        return logs.map(this::toResponse);
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        String userName = userRepo.findById(log.getUser().getId())
                .map(User::getFullName)
                .orElse("Unknown");

        return ActivityLogResponse.builder()
                .id(log.getId())
                .activityType(log.getActivityType().name())
                .description(log.getDescription())
                .entityId(log.getEntityId())
                .createdAt(log.getCreatedAt())
                .userName(userName)
                .build();
    }
}
