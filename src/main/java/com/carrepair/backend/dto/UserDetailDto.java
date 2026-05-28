package com.carrepair.backend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class UserDetailDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean isActive;
    private boolean isBlocked;
    private LocalDateTime createdAt;
    private Long statsCount;
    private String shopAddress;
    private Double latitude;
    private Double longitude;
    private List<ActivityLogDto> recentActivity;
}
