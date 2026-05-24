package com.carrepair.backend.controller.quote;


import com.carrepair.backend.dto.request.quote.SubmitQuoteRequestDto;
import com.carrepair.backend.dto.response.chat.StreamTokenResponseDto;
import com.carrepair.backend.dto.response.quote.ChatChannelResponseDto;
import com.carrepair.backend.dto.response.quote.QuoteResponseDto;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.quote.QuoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QuoteController {

    private final QuoteService quoteService;
    private final UserRepository userRepository;

    public QuoteController(QuoteService quoteService,
                           UserRepository userRepository) {
        this.quoteService = quoteService;
        this.userRepository = userRepository;
    }

    @PostMapping("/quotes")
    @PreAuthorize("hasAuthority('SHOP_OWNER')")
    public ResponseEntity<QuoteResponseDto> submitQuote(
            @RequestBody SubmitQuoteRequestDto dto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuoteResponseDto response = quoteService.submitQuote(user.getId(), dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/leads/{leadId}/quotes")
    @PreAuthorize("hasAuthority('CAR_OWNER')")
    public ResponseEntity<List<QuoteResponseDto>> getQuotesForLead(
            @PathVariable Long leadId) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<QuoteResponseDto> quotes =
                quoteService.getQuotesForLead(leadId, user.getId());

        return ResponseEntity.ok(quotes);
    }

    @PostMapping("/leads/{leadId}/quotes/{quoteId}/accept")
    @PreAuthorize("hasAuthority('CAR_OWNER')")
    public ResponseEntity<QuoteResponseDto> acceptQuote(
            @PathVariable Long leadId,
            @PathVariable Long quoteId) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuoteResponseDto response = quoteService.acceptQuote(leadId, quoteId, user.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/leads/{leadId}/chat-channel")
    @PreAuthorize("hasAnyRole('CAR_OWNER', 'SHOP_OWNER')")
    public ResponseEntity<ChatChannelResponseDto> getChatChannel(
            @PathVariable Long leadId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatChannelResponseDto response = quoteService.getChatChannel(leadId, user.getId());
        return ResponseEntity.ok(response);
    }
}