package com.carrepair.backend.service.dispute;

import com.carrepair.backend.dto.request.dispute.AdminResolveDisputeRequestDto;
import com.carrepair.backend.dto.response.dispute.DisputeResponseDto;
import com.carrepair.backend.entity.*;
import com.carrepair.backend.enums.DisputeStatus;
import com.carrepair.backend.repository.*;
import com.carrepair.backend.service.fcm.FcmService;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final LeadRepository leadRepository;
    private final QuoteRepository quoteRepository;
    private final RepairShopRepository repairShopRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @Transactional
    public DisputeResponseDto raiseDispute(Long leadId, Long carOwnerId, String reason) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (!lead.getCarOwner().getId().equals(carOwnerId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (lead.getStatus() != LeadStatus.IN_PROGRESS) {
            throw new RuntimeException("Can only dispute in-progress leads");
        }

        if (disputeRepository.findByLeadId(leadId).isPresent()) {
            throw new RuntimeException("Dispute already raised");
        }

        lead.setStatus(LeadStatus.DISPUTED);
        leadRepository.save(lead);

        Dispute dispute = Dispute.builder()
                .leadId(leadId)
                .raisedBy(carOwnerId)
                .reason(reason)
                .status(DisputeStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        disputeRepository.save(dispute);

        Quote acceptedQuote = quoteRepository
                .findByLeadIdAndStatus(leadId, QuoteStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException("No accepted quote found"));

        RepairShop shop = repairShopRepository
                .findById(acceptedQuote.getRepairShop().getId())
                .orElseThrow(() -> new RuntimeException("Repair shop not found"));

        User shopUser = userRepository.findById(shop.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Shop user not found"));

        fcmService.sendDisputeRaisedNotification(shopUser.getFcmToken(), lead.getTitle());

        List<User> admins = userRepository.findAllByRole(Role.ADMIN);
        for (User admin : admins) {
            if (admin.getFcmToken() != null) {
                fcmService.sendDisputeRaisedNotification(admin.getFcmToken(), lead.getTitle());
            }
        }

        User raisedByUser = userRepository.findById(carOwnerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDto(dispute, raisedByUser.getFullName());
    }

    @Transactional
    public DisputeResponseDto adminResolveDispute(Long disputeId,
                                                  AdminResolveDisputeRequestDto dto) {

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw new RuntimeException("Already resolved");
        }

        dispute.setStatus(DisputeStatus.valueOf(dto.getResolution()));
        dispute.setAdminNote(dto.getAdminNote());
        dispute.setResolvedAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        Lead lead = leadRepository.findById(dispute.getLeadId())
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        lead.setStatus(LeadStatus.COMPLETED);
        lead.setCompletedAt(LocalDateTime.now());
        leadRepository.save(lead);

        User carOwner = userRepository.findById(lead.getCarOwner().getId())
                .orElseThrow(() -> new RuntimeException("Car owner not found"));

        Quote acceptedQuote = quoteRepository
                .findByLeadIdAndStatus(lead.getId(), QuoteStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException("No accepted quote found"));

        RepairShop shop = repairShopRepository
                .findById(acceptedQuote.getRepairShop().getId())
                .orElseThrow(() -> new RuntimeException("Repair shop not found"));

        User shopUser = userRepository.findById(shop.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Shop user not found"));

        fcmService.sendDisputeResolvedNotification(carOwner.getFcmToken(), dto.getResolution());
        fcmService.sendDisputeResolvedNotification(shopUser.getFcmToken(), dto.getResolution());

        User raisedByUser = userRepository.findById(dispute.getRaisedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDto(dispute, raisedByUser.getFullName());
    }

    public List<DisputeResponseDto> getAllDisputes() {
        return disputeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(dispute -> {
                    User raisedByUser = userRepository.findById(dispute.getRaisedBy())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return mapToDto(dispute, raisedByUser.getFullName());
                })
                .collect(Collectors.toList());
    }

    public DisputeResponseDto getDisputeByLead(Long leadId) {
        Dispute dispute = disputeRepository.findByLeadId(leadId)
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        User raisedByUser = userRepository.findById(dispute.getRaisedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDto(dispute, raisedByUser.getFullName());
    }

    private DisputeResponseDto mapToDto(Dispute dispute, String raisedByName) {
        return DisputeResponseDto.builder()
                .id(dispute.getId())
                .leadId(dispute.getLeadId())
                .raisedByName(raisedByName)
                .reason(dispute.getReason())
                .status(dispute.getStatus().name())
                .adminNote(dispute.getAdminNote())
                .createdAt(dispute.getCreatedAt() != null
                        ? dispute.getCreatedAt().toString() : null)
                .resolvedAt(dispute.getResolvedAt() != null
                        ? dispute.getResolvedAt().toString() : null)
                .build();
    }
}
