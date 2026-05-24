package com.carrepair.backend.controller.chat;


import com.carrepair.backend.dto.response.chat.StreamTokenResponseDto;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.service.StreamChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class StreamChatController {

    private final StreamChatService streamChatService;
    private final UserRepository userRepository;

    @GetMapping("/token")
    @PreAuthorize("hasAnyRole('CAR_OWNER', 'SHOP_OWNER')")
    public ResponseEntity<StreamTokenResponseDto> getChatToken() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = streamChatService.generateUserToken(user.getId());

        return ResponseEntity.ok(new StreamTokenResponseDto(token, null));
    }
}
