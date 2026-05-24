package com.carrepair.backend.dto.response.chat;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserMeResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String role;
}
