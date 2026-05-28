package com.carrepair.backend.dto.response.dashboard;

import lombok.*;
import java.time.LocalDateTime;

@Builder @Getter
public class ActivityLogResponse {
    private Long id;
    private String activityType;
    private String description;
    private Long entityId;
    private LocalDateTime createdAt;
    private String userName;
}
