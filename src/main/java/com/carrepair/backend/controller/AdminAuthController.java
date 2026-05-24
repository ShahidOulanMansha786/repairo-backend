package com.carrepair.backend.controller;

import com.carrepair.backend.dto.request.AdminLoginRequestDto;
import com.carrepair.backend.dto.response.AdminAuthResponseDto;
import com.carrepair.backend.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AdminAuthResponseDto> login(@RequestBody AdminLoginRequestDto dto) {
        return ResponseEntity.ok(adminAuthService.login(dto));
    }
}