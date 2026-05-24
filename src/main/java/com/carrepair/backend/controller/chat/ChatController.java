package com.carrepair.backend.controller.chat;



import com.carrepair.backend.dto.request.chat.ChatNotifyRequestDto;
import com.carrepair.backend.dto.response.MessageResponseDto;
import com.carrepair.backend.entity.Lead;
import com.carrepair.backend.entity.Quote;
import com.carrepair.backend.entity.QuoteStatus;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.repository.LeadRepository;
import com.carrepair.backend.repository.QuoteRepository;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final LeadRepository leadRepository;
    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @PostMapping("/notify")
    public ResponseEntity<MessageResponseDto> sendChatNotification(
            @RequestBody ChatNotifyRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User sender = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String channelId = dto.getChannelId();
        Long leadId = Long.parseLong(channelId.replace("lead_", ""));

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        Quote acceptedQuote = quoteRepository.findByLeadIdAndStatus(leadId, QuoteStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException("No accepted quote found"));

        Long carOwnerId = lead.getCarOwner().getId();
        Long repairShopUserId = acceptedQuote.getRepairShop().getUser().getId();

        User receiver;
        if (sender.getId().equals(carOwnerId)) {
            receiver = acceptedQuote.getRepairShop().getUser();
        } else {
            receiver = lead.getCarOwner();
        }

        if (receiver.getFcmToken() != null) {
            fcmService.sendChatMessageNotification(
                    receiver.getFcmToken(),
                    dto.getSenderName(),
                    dto.getMessagePreview(),
                    dto.getChannelId()
            );
        }

        return ResponseEntity.ok(new MessageResponseDto("Notification sent"));
    }
}