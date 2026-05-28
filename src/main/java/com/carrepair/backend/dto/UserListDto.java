package com.carrepair.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class UserListDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean isActive;
    private boolean isBlocked;
    private LocalDateTime createdAt;
}
