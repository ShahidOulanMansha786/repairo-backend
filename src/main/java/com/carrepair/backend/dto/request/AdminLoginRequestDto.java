package com.carrepair.backend.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginRequestDto {
    private String email;
    private String password;
}
