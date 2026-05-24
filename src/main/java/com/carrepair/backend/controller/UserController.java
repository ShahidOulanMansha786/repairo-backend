package com.carrepair.backend.controller;



import com.carrepair.backend.dto.request.fcm.FcmTokenRequestDto;
import com.carrepair.backend.dto.response.MessageResponseDto;
import com.carrepair.backend.dto.response.chat.UserMeResponseDto;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/fcm-token")
    public ResponseEntity<MessageResponseDto> updateFcmToken(@RequestBody FcmTokenRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updateFcmToken(email, dto.getFcmToken());
        return ResponseEntity.ok(new MessageResponseDto("FCM token updated"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(new UserMeResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        ));
    }
}
