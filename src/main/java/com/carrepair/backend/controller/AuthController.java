package com.carrepair.backend.controller;


import com.carrepair.backend.dto.request.*;
import com.carrepair.backend.dto.request.repairshop.ShopLoginRequestDto;
import com.carrepair.backend.dto.request.repairshop.ShopLoginResponseDto;
import com.carrepair.backend.dto.request.repairshop.ShopOtpRequestDto;
import com.carrepair.backend.dto.request.repairshop.ShopVerifyOtpRequestDto;
import com.carrepair.backend.dto.response.AccessTokenResponseDto;
import com.carrepair.backend.dto.response.AuthResponseDto;
import com.carrepair.backend.dto.response.MessageResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopAuthResponseDto;
import com.carrepair.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDto> verifyOtp(@RequestBody VerifyOtpRequestDto dto) {
        return ResponseEntity.ok(authService.verifyOtp(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDto> refresh(@RequestBody RefreshTokenRequestDto dto) {
        return ResponseEntity.ok(authService.refresh(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logout(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.logout(authHeader));
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponseDto> signup(
            @RequestBody SignupRequestDto dto,
            HttpServletRequest request) {

        String ipAddress = getClientIp(request);
        return ResponseEntity.ok(authService.signup(dto, ipAddress));
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponseDto> login(@RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponseDto> resendOtp(@RequestBody ResendOtpRequestDto dto) {
        return ResponseEntity.ok(authService.resendOtp(dto));
    }

    @PostMapping("/shop/request-otp")
    public ResponseEntity<MessageResponseDto> requestOtpForShop(
            @RequestBody ShopOtpRequestDto dto,
            HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        return ResponseEntity.ok(authService.requestOtpForShop(dto, ipAddress));
    }

    @PostMapping("/shop/verify-otp")
    public ResponseEntity<ShopAuthResponseDto> verifyOtpForShop(@RequestBody ShopVerifyOtpRequestDto dto) {
        return ResponseEntity.ok(authService.verifyOtpForShop(dto));
    }

    @PostMapping("/shop/login/request-otp")
    public ResponseEntity<MessageResponseDto> shopLoginRequestOtp(
            @RequestBody ShopLoginRequestDto dto,
            HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        return ResponseEntity.ok(authService.requestLoginOtpForShop(dto, ipAddress));
    }

    @PostMapping("/shop/login/verify-otp")
    public ResponseEntity<ShopLoginResponseDto> shopLoginVerifyOtp(
            @RequestBody ShopVerifyOtpRequestDto dto) {
        return ResponseEntity.ok(authService.verifyLoginOtpForShop(dto));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
