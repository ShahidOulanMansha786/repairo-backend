package com.carrepair.backend.service.quote;



import com.carrepair.backend.dto.response.chat.StreamTokenResponseDto;
import com.carrepair.backend.dto.request.quote.SubmitQuoteRequestDto;
import com.carrepair.backend.dto.response.quote.ChatChannelResponseDto;
import com.carrepair.backend.dto.response.quote.QuoteResponseDto;
import com.carrepair.backend.entity.*;
import com.carrepair.backend.enums.ActivityType;
import com.carrepair.backend.repository.*;
import com.carrepair.backend.repository.RepairShopRepository;
import com.carrepair.backend.service.ActivityLogService;
import com.carrepair.backend.service.ApprovalStatus;
import com.carrepair.backend.service.S3Service;
import com.carrepair.backend.service.chat.FirestoreChatService;
import com.carrepair.backend.service.fcm.FcmService;
import com.carrepair.backend.service.service.StreamChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final RepairShopRepository repairShopRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final SimpMessagingTemplate messagingTemplate;
    private final FcmService fcmService;
    private final FirestoreChatService firestoreChatService;
    private final ActivityLogService activityLogService;



    @Transactional
    public QuoteResponseDto submitQuote(Long userId, SubmitQuoteRequestDto dto) {
        RepairShop shop = repairShopRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        if (shop.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new RuntimeException("Shop not approved");
        }

        Lead lead = leadRepository.findById(dto.getLeadId())
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (lead.getStatus() != LeadStatus.OPEN) {
            throw new RuntimeException("Lead is no longer open");
        }

        if (quoteRepository.existsByLeadIdAndRepairShopId(dto.getLeadId(), shop.getId())) {
            throw new RuntimeException("Already submitted a quote");
        }

        Quote quote = Quote.builder()
                .lead(lead)
                .repairShop(shop)
                .price(dto.getPrice())
                .message(dto.getMessage())
                .status(QuoteStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Quote savedQuote = quoteRepository.save(quote);

        activityLogService.log(shop.getUser().getId(), ActivityType.QUOTE_SUBMITTED,
                "Submitted a quote for lead #" + lead.getId(), savedQuote.getId());

        String shopLogoUrl = null;
        if (shop.getLogoUrl() != null) {
            shopLogoUrl = s3Service.generateDownloadPresignedUrl(shop.getLogoUrl());
        }

        QuoteResponseDto quoteResponseDto = QuoteResponseDto.builder()
                .id(savedQuote.getId())
                .leadId(savedQuote.getLead().getId())
                .repairShopId(savedQuote.getRepairShop().getId())
                .shopName(shop.getShopName())
                .shopLogoUrl(shopLogoUrl)
                .price(savedQuote.getPrice())
                .message(savedQuote.getMessage())
                .status(savedQuote.getStatus().name())
                .createdAt(savedQuote.getCreatedAt())
                .build();

        User carOwner = userRepository.findById(lead.getCarOwner().getId())
                .orElseThrow(() -> new RuntimeException("Car owner not found"));

        messagingTemplate.convertAndSend(
                "/topic/leads/" + dto.getLeadId() + "/quotes",
                quoteResponseDto
        );

        return quoteResponseDto;
    }

    public List<QuoteResponseDto> getQuotesForLead(Long leadId, Long carOwnerId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (!lead.getCarOwner().getId().equals(carOwnerId)) {
            throw new RuntimeException("Unauthorized");
        }

        List<Quote> quotes = quoteRepository.findByLeadIdOrderByCreatedAtDesc(leadId);

        return quotes.stream().map(quote -> {
            RepairShop shop = quote.getRepairShop();

            String shopLogoUrl = null;
            if (shop.getLogoUrl() != null) {
                shopLogoUrl = s3Service.generateDownloadPresignedUrl(shop.getLogoUrl());
            }

            return QuoteResponseDto.builder()
                    .id(quote.getId())
                    .leadId(quote.getLead().getId())
                    .repairShopId(shop.getId())
                    .shopName(shop.getShopName())
                    .shopLogoUrl(shopLogoUrl)
                    .price(quote.getPrice())
                    .message(quote.getMessage())
                    .status(quote.getStatus().name())
                    .createdAt(quote.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public QuoteResponseDto acceptQuote(Long leadId, Long quoteId, Long carOwnerId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (!lead.getCarOwner().getId().equals(carOwnerId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (lead.getStatus() != LeadStatus.OPEN) {
            throw new RuntimeException("Lead is no longer open");
        }

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        if (!quote.getLead().getId().equals(leadId)) {
            throw new RuntimeException("Quote does not belong to this lead");
        }

        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new RuntimeException("Quote already processed");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quoteRepository.save(quote);

        List<Quote> otherPendingQuotes = quoteRepository.findByLeadIdOrderByCreatedAtDesc(leadId)
                .stream()
                .filter(q -> !q.getId().equals(quoteId) && q.getStatus() == QuoteStatus.PENDING)
                .collect(Collectors.toList());

        otherPendingQuotes.forEach(q -> q.setStatus(QuoteStatus.REJECTED));
        quoteRepository.saveAll(otherPendingQuotes);

        lead.setStatus(LeadStatus.IN_PROGRESS);
        lead.setInProgressAt(LocalDateTime.now());
        leadRepository.save(lead);

        RepairShop acceptedShop = quote.getRepairShop();
        User carOwner = lead.getCarOwner();
        User shopUser = acceptedShop.getUser();

        String channelId = firestoreChatService.createChatChannel(
                lead.getId(),
                carOwner.getId(),
                carOwner.getFullName(),
                acceptedShop.getUser().getId(),
                shopUser.getFullName(),
                lead.getTitle()
        );

        User acceptedUser = acceptedShop.getUser();
        if (acceptedUser.getFcmToken() != null) {
            log.info("Sending FCM to accepted shop. Token: {}", acceptedUser.getFcmToken());
            fcmService.sendQuoteAcceptedNotification(
                    acceptedUser.getFcmToken(),
                    acceptedShop.getShopName(),
                    lead.getTitle()
            );
        }

        for (Quote rejectedQuote : otherPendingQuotes) {
            RepairShop rejectedShop = rejectedQuote.getRepairShop();
            User rejectedUser = rejectedShop.getUser();
            if (rejectedUser.getFcmToken() != null) {
                log.info("Sending FCM to rejected shop. Token: {}", rejectedUser.getFcmToken());
                fcmService.sendQuoteRejectedNotification(
                        rejectedUser.getFcmToken(),
                        rejectedShop.getShopName(),
                        lead.getTitle()
                );
            }
        }

        String shopLogoUrl = null;
        if (acceptedShop.getLogoUrl() != null) {
            shopLogoUrl = s3Service.generateDownloadPresignedUrl(acceptedShop.getLogoUrl());
        }

        activityLogService.log(lead.getCarOwner().getId(), ActivityType.QUOTE_ACCEPTED,
                "Accepted a quote for lead: " + lead.getTitle(), quote.getId());

        return QuoteResponseDto.builder()
                .id(quote.getId())
                .leadId(quote.getLead().getId())
                .repairShopId(acceptedShop.getId())
                .shopName(acceptedShop.getShopName())
                .shopLogoUrl(shopLogoUrl)
                .price(quote.getPrice())
                .message(quote.getMessage())
                .status(quote.getStatus().name())
                .createdAt(quote.getCreatedAt())
                .channelId(channelId)
                .build();
    }

//    public StreamTokenResponseDto getChannelInfo(Long leadId, Long userId) {
//        leadRepository.findById(leadId)
//                .orElseThrow(() -> new RuntimeException("Lead not found"));
//
//        Quote acceptedQuote = quoteRepository.findByLeadIdAndStatus(leadId, QuoteStatus.ACCEPTED)
//                .orElseThrow(() -> new RuntimeException("No active chat for this lead"));
//
//        String token = streamChatService.generateUserToken(userId);
//
//        return new StreamTokenResponseDto(token, "lead-" + leadId);
//    }

    public ChatChannelResponseDto getChatChannel(Long leadId, Long requestingUserId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        Quote acceptedQuote = quoteRepository.findByLeadIdAndStatus(leadId, QuoteStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException("No active chat channel for this lead"));

        // Only car owner or the accepted repair shop's user can access
        Long carOwnerId = lead.getCarOwner().getId();
        Long repairShopUserId = acceptedQuote.getRepairShop().getUser().getId();

        if (!requestingUserId.equals(carOwnerId) && !requestingUserId.equals(repairShopUserId)) {
            throw new RuntimeException("You are not a participant of this chat");
        }

        return ChatChannelResponseDto.builder()
                .channelId("lead_" + leadId)
                .build();
    }
}
