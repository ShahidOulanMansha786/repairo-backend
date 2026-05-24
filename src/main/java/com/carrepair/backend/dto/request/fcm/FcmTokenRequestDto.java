package com.carrepair.backend.dto.request.fcm;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequestDto {
    private String fcmToken;
}