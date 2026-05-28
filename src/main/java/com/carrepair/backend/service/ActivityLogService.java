package com.carrepair.backend.service;

import com.carrepair.backend.entity.ActivityLog;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.enums.ActivityType;
import com.carrepair.backend.repository.ActivityLogRepository;
import com.carrepair.backend.repository.UserRepository;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public void log(Long userId, ActivityType type, String description, Long entityId) {
        User user = userRepository.getReferenceById(userId);
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .activityType(type)
                .description(description)
                .entityId(entityId)
                .createdAt(LocalDateTime.now())
                .build();
        activityLogRepository.save(log);
    }
}
