package com.carrepair.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ActivityLogDto {
    private String activityType;
    private String description;
    private Long entityId;
    private LocalDateTime createdAt;
}
