package com.carrepair.backend.service;


import com.carrepair.backend.dto.redis.PendingUserDto;
import com.carrepair.backend.dto.request.*;
import com.carrepair.backend.dto.request.repairshop.ShopLoginRequestDto;
import com.carrepair.backend.dto.request.repairshop.ShopLoginResponseDto;
import com.carrepair.backend.dto.request.repairshop.ShopOtpRequestDto;
import com.carrepair.backend.dto.request.repairshop.ShopVerifyOtpRequestDto;
import com.carrepair.backend.dto.response.AccessTokenResponseDto;
import com.carrepair.backend.dto.response.AuthResponseDto;
import com.carrepair.backend.dto.response.MessageResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopAuthResponseDto;
import com.carrepair.backend.entity.RefreshToken;
import com.carrepair.backend.entity.RepairShop;
import com.carrepair.backend.entity.Role;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.repository.RefreshTokenRepository;
import com.carrepair.backend.repository.RepairShopRepository;
import com.carrepair.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailValidationService emailValidationService;
    private final PhoneNumberValidator phoneNumberValidator;
    private final RateLimiterService rateLimiterService;
    private final RepairShopRepository repairShopRepository;
    private final RedisTemplate<String, Object> redisTemplate;


    @Transactional
    public MessageResponseDto signup(SignupRequestDto dto, String ipAddress) {
        rateLimiterService.check("signup", ipAddress, 5, 60);

        if (!phoneNumberValidator.isValidPakistaniNumber(dto.getPhone())) {
            throw new RuntimeException("Please enter a valid Pakistani mobile number.");
        }

        if (emailValidationService.isDisposable(dto.getEmail())) {
            throw new RuntimeException("Disposable email addresses are not allowed.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered.");
        }

        if(userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Phone number already registered.");
        }

        Role role;
        try {
            role = Role.valueOf(dto.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role. Must be CAR_OWNER or SHOP_OWNER");
        }

        if (role == Role.ADMIN) {
            throw new RuntimeException("Cannot sign up as ADMIN");
        }

        PendingUserDto pendingUser = PendingUserDto.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(dto.getRole())
                .build();

        otpService.generateAndSendOtp(dto.getEmail(), pendingUser);

        return new MessageResponseDto("OTP sent to your email.");
    }

    @Transactional
    public MessageResponseDto resendOtp(ResendOtpRequestDto dto) {
        otpService.resendOtp(dto.getEmail());
        return new MessageResponseDto("A new OTP has been sent to your email.");
    }


    @Transactional
    public MessageResponseDto login(LoginRequestDto dto) {

        rateLimiterService.check("login", dto.getEmail(), 5, 15);

        if (!userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("No account found with this email.");
        }

        otpService.generateAndSendOtp(dto.getEmail());

        return new MessageResponseDto("OTP sent to your email.");
    }


    @Transactional
    public AuthResponseDto verifyOtp(VerifyOtpRequestDto dto) {

        otpService.validateOtp(dto.getEmail(), dto.getOtp());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseGet(() -> {

                    PendingUserDto pending = otpService.getPendingUser(dto.getEmail());

                    Role role;
                    try {
                        role = Role.valueOf(pending.getRole());
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid role found in session. Please sign up again.");
                    }

                    User newUser = User.builder()
                            .fullName(pending.getFullName())
                            .email(pending.getEmail())
                            .phone(pending.getPhone())
                            .role(role)
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .build();

                    User savedUser = userRepository.save(newUser);

                    otpService.deletePendingUser(dto.getEmail());

                    return savedUser;
                });

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

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .role(user.getRole().name())
                .build();
    }


    @Transactional
    public AccessTokenResponseDto refresh(RefreshTokenRequestDto dto) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(dto.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getIsRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (LocalDateTime.now().isAfter(refreshToken.getExpiresAt())) {
            throw new RuntimeException("Refresh token has expired");
        }

        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshTokenValue = jwtService.generateRefreshToken();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenValue)
                .isRevoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return AccessTokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .build();
    }


    @Transactional
    public MessageResponseDto logout(String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.deleteByUserId(user.getId());

        return MessageResponseDto.builder()
                .message("Logged out successfully")
                .build();
    }


    @Transactional
    public MessageResponseDto requestOtpForShop(ShopOtpRequestDto dto, String ipAddress) {
        rateLimiterService.check("signup", ipAddress, 5, 60);

        if (!phoneNumberValidator.isValidPakistaniNumber(dto.getPhone())) {
            throw new RuntimeException("Please enter a valid Pakistani mobile number.");
        }

        if (emailValidationService.isDisposable(dto.getEmail())) {
            throw new RuntimeException("Disposable email addresses are not allowed.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.info("User with email {} already exists", dto.getEmail());
            throw new RuntimeException("Email already registered.");
        }

        if(userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Phone number already registered.");
        }

        PendingShopRegistrationDto pendingShop = PendingShopRegistrationDto.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .shopName(dto.getShopName())
                .description(dto.getDescription())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();

        otpService.generateAndSendOtpForShop(dto.getEmail(), pendingShop);

        return new MessageResponseDto("OTP sent to your email.");
    }

    @Transactional
    public ShopAuthResponseDto verifyOtpForShop(ShopVerifyOtpRequestDto dto) {

        otpService.validateOtp(dto.getEmail(), dto.getOtp());

        Object raw = redisTemplate.opsForValue().get("pending_shop:" + dto.getEmail());
        if (raw == null) {
            throw new RuntimeException("Registration data expired. Please sign up again.");
        }
        ObjectMapper mapper = new ObjectMapper();
        PendingShopRegistrationDto pending = mapper.convertValue(raw, PendingShopRegistrationDto.class);

        User user = User.builder()
                .fullName(pending.getFullName())
                .email(pending.getEmail())
                .phone(pending.getPhone())
                .role(Role.SHOP_OWNER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        Point location = new GeometryFactory(new PrecisionModel(), 4326)
                .createPoint(new Coordinate(pending.getLongitude(), pending.getLatitude()));

        RepairShop shop = RepairShop.builder()
                .user(user)
                .shopName(pending.getShopName())
                .description(pending.getDescription())
                .phone(pending.getPhone())
                .address(pending.getAddress())
                .location(location)
                .approvalStatus(ApprovalStatus.INCOMPLETE)
                .isVerified(false)
                .isActive(true)
                .logoUrl(null)
                .cnicUrl(null)
                .businessDocUrl(null)
                .createdAt(LocalDateTime.now())
                .build();
        repairShopRepository.save(shop);

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

        redisTemplate.delete("otp:" + dto.getEmail());
        redisTemplate.delete("pending_shop:" + dto.getEmail());

        return ShopAuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .role(user.getRole().name())
                .build();
    }


    @Transactional
    public MessageResponseDto requestLoginOtpForShop(ShopLoginRequestDto dto, String ipAddress) {
        rateLimiterService.check("shop-login", ipAddress, 5, 15);

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email."));

        if (user.getRole() != Role.SHOP_OWNER) {
            throw new RuntimeException("No repair shop account found with this email.");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("Your account has been deactivated. Please contact support.");
        }

        otpService.generateAndSendOtp(dto.getEmail());

        return new MessageResponseDto("OTP sent to your email.");
    }

    @Transactional
    public ShopLoginResponseDto verifyLoginOtpForShop(ShopVerifyOtpRequestDto dto) {
        otpService.validateOtp(dto.getEmail(), dto.getOtp());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Account not found. Please sign up again."));

        RepairShop shop = repairShopRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Repair shop profile not found."));

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

        return ShopLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .role(user.getRole().name())
                .approvalStatus(shop.getApprovalStatus().name())
                .rejectionReason(shop.getRejectionReason())
                .build();
    }
}