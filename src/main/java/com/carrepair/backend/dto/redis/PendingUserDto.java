package com.carrepair.backend.dto.redis;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingUserDto {
    private String fullName;
    private String email;
    private String phone;
    private String role;
}