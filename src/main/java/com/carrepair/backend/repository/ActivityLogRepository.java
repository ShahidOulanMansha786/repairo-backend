package com.carrepair.backend.repository;

import com.carrepair.backend.entity.ActivityLog;
import com.carrepair.backend.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ActivityLog> findByActivityTypeOrderByCreatedAtDesc(
            ActivityType type, Pageable pageable);
}
