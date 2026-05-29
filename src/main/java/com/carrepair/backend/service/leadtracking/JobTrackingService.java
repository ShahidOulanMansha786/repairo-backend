package com.carrepair.backend.service.leadtracking;

import com.carrepair.backend.dto.request.dispute.JobTrackingResponseDto;
import com.carrepair.backend.entity.*;
import com.carrepair.backend.repository.LeadRepository;
import com.carrepair.backend.repository.QuoteRepository;
import com.carrepair.backend.repository.RepairShopRepository;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobTrackingService {

    private final LeadRepository leadRepository;
    private final RepairShopRepository repairShopRepository;
    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @Transactional
    public JobTrackingResponseDto shopMarksDone(Long leadId, Long userId) {
        RepairShop shop = repairShopRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Repair shop not found"));

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        Quote acceptedQuote = quoteRepository
                .findByLeadIdAndStatus(leadId, QuoteStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException("No accepted quote found"));

        if (!acceptedQuote.getRepairShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (lead.getStatus() != LeadStatus.IN_PROGRESS) {
            throw new RuntimeException("Lead is not in progress");
        }

        if (lead.getShopMarkedDone()) {
            throw new RuntimeException("Already marked done");
        }

        lead.setShopMarkedDone(true);

        if (Boolean.TRUE.equals(lead.getOwnerMarkedSatisfied())) {
            completeLead(lead);
        }

        leadRepository.save(lead);

        User carOwner = userRepository.findById(lead.getCarOwner().getId())
                .orElseThrow(() -> new RuntimeException("Car owner not found"));

        fcmService.sendShopMarkedDoneNotification(
                carOwner.getFcmToken(), lead.getTitle());

        return mapToDto(lead);
    }

    @Transactional
    public JobTrackingResponseDto ownerMarksSatisfied(Long leadId, Long carOwnerId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (!lead.getCarOwner().getId().equals(carOwnerId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (lead.getStatus() != LeadStatus.IN_PROGRESS) {
            throw new RuntimeException("Lead is not in progress");
        }

        if (Boolean.TRUE.equals(lead.getOwnerMarkedSatisfied())) {
            throw new RuntimeException("Already marked satisfied");
        }

        lead.setOwnerMarkedSatisfied(true);

        if (Boolean.TRUE.equals(lead.getShopMarkedDone())) {
            completeLead(lead);
        }

        leadRepository.save(lead);

        return mapToDto(lead);
    }

    private void completeLead(Lead lead) {
        lead.setStatus(LeadStatus.COMPLETED);
        lead.setCompletedAt(LocalDateTime.now());
        leadRepository.save(lead);

        User carOwner = userRepository.findById(lead.getCarOwner().getId())
                .orElseThrow(() -> new RuntimeException("Car owner not found"));

        Quote acceptedQuote = quoteRepository
                .findByLeadIdAndStatus(lead.getId(), QuoteStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException("No accepted quote found"));

        RepairShop shop = repairShopRepository.findById(acceptedQuote.getRepairShop().getId())
                .orElseThrow(() -> new RuntimeException("Repair shop not found"));

        User shopUser = userRepository.findById(shop.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Shop user not found"));

        fcmService.sendJobCompletedNotification(carOwner.getFcmToken(), lead.getTitle());
        fcmService.sendJobCompletedNotification(shopUser.getFcmToken(), lead.getTitle());
    }

    public JobTrackingResponseDto getJobStatus(Long leadId, Long userId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        boolean isCarOwner = lead.getCarOwner().getId().equals(userId);

        boolean isAcceptedShop = false;
        Optional<Quote> acceptedQuote = quoteRepository
                .findByLeadIdAndStatus(leadId, QuoteStatus.ACCEPTED);

        if (acceptedQuote.isPresent()) {
            RepairShop shop = repairShopRepository
                    .findById(acceptedQuote.get().getRepairShop().getId())
                    .orElse(null);
            if (shop != null && shop.getUser().getId().equals(userId)) {
                isAcceptedShop = true;
            }
        }

        if (!isCarOwner && !isAcceptedShop) {
            throw new RuntimeException("Unauthorized");
        }

        return mapToDto(lead);
    }

    private JobTrackingResponseDto mapToDto(Lead lead) {
        return JobTrackingResponseDto.builder()
                .leadId(lead.getId())
                .status(lead.getStatus().name())
                .shopMarkedDone(lead.getShopMarkedDone())
                .ownerMarkedSatisfied(lead.getOwnerMarkedSatisfied())
                .inProgressAt(lead.getInProgressAt() != null
                        ? lead.getInProgressAt().toString() : null)
                .completedAt(lead.getCompletedAt() != null
                        ? lead.getCompletedAt().toString() : null)
                .build();
    }
}
