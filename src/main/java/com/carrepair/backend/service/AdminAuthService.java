package com.carrepair.backend.service;


import com.carrepair.backend.dto.request.AdminLoginRequestDto;
import com.carrepair.backend.dto.response.AdminAuthResponseDto;
import com.carrepair.backend.entity.AdminCredentials;
import com.carrepair.backend.entity.RefreshToken;
import com.carrepair.backend.entity.Role;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.repository.AdminCredentialsRepository;
import com.carrepair.backend.repository.RefreshTokenRepository;
import com.carrepair.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final UserRepository userRepository;
    private final AdminCredentialsRepository adminCredentialsRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AdminAuthResponseDto login(AdminLoginRequestDto dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Invalid credentials");
        }

        AdminCredentials credentials = adminCredentialsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), credentials.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        credentials.setLastLoginAt(LocalDateTime.now());
        adminCredentialsRepository.save(credentials);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .isRevoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AdminAuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .role(user.getRole().name())
                .build();
    }
}
