package com.carrepair.backend.service;

import com.carrepair.backend.dto.redis.PendingUserDto;
import com.carrepair.backend.dto.request.PendingShopRegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MailService mailService;
    private final RateLimiterService rateLimiterService;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final String OTP_PREFIX          = "otp:";
    private static final String PENDING_USER_PREFIX = "pending_user:";
    private static final long   TTL_MINUTES         = 10;
    private static final String PENDING_SHOP_PREFIX = "pending_shop:";


    public void generateAndSendOtp(String email, PendingUserDto pendingUser) {
        String otpCode = generateOtpCode();

        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otpCode,
                TTL_MINUTES,
                TimeUnit.MINUTES
        );

        redisTemplate.opsForValue().set(
                PENDING_USER_PREFIX + email,
                pendingUser,
                TTL_MINUTES,
                TimeUnit.MINUTES
        );

        mailService.sendOtpEmail(email, otpCode);
    }

    public void generateAndSendOtpForShop(String email, PendingShopRegistrationDto pendingShop) {
        String otpCode = generateOtpCode();

        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otpCode,
                TTL_MINUTES,
                TimeUnit.MINUTES
        );

        redisTemplate.opsForValue().set(
                PENDING_SHOP_PREFIX + email,
                pendingShop,
                TTL_MINUTES,
                TimeUnit.MINUTES
        );

        mailService.sendOtpEmail(email, otpCode);
    }


    public void generateAndSendOtp(String email) {
        String otpCode = generateOtpCode();

        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otpCode,
                TTL_MINUTES,
                TimeUnit.MINUTES
        );

        mailService.sendOtpEmail(email, otpCode);
    }


    public void validateOtp(String email, String otpCode) {
        Object stored = redisTemplate.opsForValue().get(OTP_PREFIX + email);

        if (stored == null) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!stored.toString().equals(otpCode)) {
            throw new RuntimeException("Invalid OTP. Please try again.");
        }

        redisTemplate.delete(OTP_PREFIX + email);
    }


    public PendingUserDto getPendingUser(String email) {
        Object data = redisTemplate.opsForValue().get(PENDING_USER_PREFIX + email);

        if (data == null) {
            throw new RuntimeException("Signup session expired. Please sign up again.");
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(data, PendingUserDto.class);
    }

    public void resendOtp(String email) {

        rateLimiterService.check("resend-otp", email, 3, 10);  // 3 attempts per 10 min

        boolean isExistingUser = redisTemplate.hasKey(OTP_PREFIX + email);

        boolean hasPendingSignup = redisTemplate.hasKey(PENDING_USER_PREFIX + email);

        boolean isRegisteredUser = hasPendingSignup || isExistingUser;

        if (!isRegisteredUser) {
            throw new RuntimeException("No active session found for this email. Please signup or login again.");
        }

        String otpCode = generateOtpCode();

        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otpCode,
                TTL_MINUTES,
                TimeUnit.MINUTES
        );

        mailService.sendOtpEmail(email, otpCode);
    }


    public void deletePendingUser(String email) {
        redisTemplate.delete(PENDING_USER_PREFIX + email);
    }


    private String generateOtpCode() {
        int otpNumber = secureRandom.nextInt(1_000_000);
        return String.format("%06d", otpNumber);
    }
}