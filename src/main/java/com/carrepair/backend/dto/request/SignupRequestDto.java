package com.carrepair.backend.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {
    private String fullName;
    private String email;
    private String phone;
    private String role;
}