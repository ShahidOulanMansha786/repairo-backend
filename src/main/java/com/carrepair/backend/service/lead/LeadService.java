package com.carrepair.backend.service.lead;


import com.carrepair.backend.dto.request.lead.CreateLeadRequestDto;
import com.carrepair.backend.dto.response.lead.AdminLeadResponseDto;
import com.carrepair.backend.dto.response.lead.LeadResponseDto;
import com.carrepair.backend.entity.Lead;
import com.carrepair.backend.entity.LeadImage;
import com.carrepair.backend.entity.LeadStatus;
import com.carrepair.backend.entity.Role;
import com.carrepair.backend.enums.ActivityType;
import com.carrepair.backend.repository.LeadImageRepository;
import com.carrepair.backend.repository.LeadRepository;
import com.carrepair.backend.repository.RepairShopRepository;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.ActivityLogService;
import com.carrepair.backend.service.fcm.FcmService;
import com.carrepair.backend.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final LeadImageRepository leadImageRepository;
    private final RepairShopRepository repairShopRepository;
    private final FcmService fcmService;
    private final S3Service s3Service;
    private final ActivityLogService  activityLogService;

    @Transactional
    public LeadResponseDto createLead(Long carOwnerId, CreateLeadRequestDto dto) {

        var user = userRepository.findById(carOwnerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Point location = new GeometryFactory(new PrecisionModel(), 4326)
                .createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));

        Lead lead = Lead.builder()
                .carOwner(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .carMake(dto.getCarMake())
                .carModel(dto.getCarModel())
                .carYear(dto.getCarYear())
                .address(dto.getAddress())
                .location(location)
                .status(LeadStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        lead = leadRepository.save(lead);

        if (dto.getImageKeys() != null) {
            for (String imageKey : dto.getImageKeys()) {
                LeadImage leadImage = LeadImage.builder()
                        .lead(lead)
                        .imageUrl(imageKey)
                        .uploadedAt(LocalDateTime.now())
                        .build();
                leadImageRepository.save(leadImage);
            }
        }

        var nearbyShops = repairShopRepository.findVerifiedShopsWithinDistance(
                dto.getLatitude(), dto.getLongitude(), 25000);

        for (var shop : nearbyShops) {
            var shopUser = shop.getUser();
            if (shopUser.getFcmToken() != null) {
                fcmService.sendNewLeadNotification(shopUser.getFcmToken(), lead);
            }
        }

        List<String> imageUrls = dto.getImageKeys() == null ? List.of() :
                dto.getImageKeys().stream()
                        .map(s3Service::generateDownloadPresignedUrl)
                        .collect(Collectors.toList());

        activityLogService.log(carOwnerId, ActivityType.LEAD_POSTED,
                "Posted a lead: " + lead.getTitle(), lead.getId());

        return LeadResponseDto.builder()
                .id(lead.getId())
                .title(lead.getTitle())
                .description(lead.getDescription())
                .carMake(lead.getCarMake())
                .carModel(lead.getCarModel())
                .carYear(lead.getCarYear())
                .address(lead.getAddress())
                .status(lead.getStatus().name())
                .createdAt(lead.getCreatedAt())
                .expiresAt(lead.getExpiresAt())
                .imageUrls(imageUrls)
                .build();
    }

    public List<LeadResponseDto> getMyLeads(Long carOwnerId) {

        List<Lead> leads = leadRepository.findByCarOwnerIdOrderByCreatedAtDesc(carOwnerId);

        return leads.stream().map(lead -> {
            List<String> imageUrls = leadImageRepository.findByLeadId(lead.getId())
                    .stream()
                    .map(image -> s3Service.generateDownloadPresignedUrl(image.getImageUrl()))
                    .collect(Collectors.toList());

            return LeadResponseDto.builder()
                    .id(lead.getId())
                    .title(lead.getTitle())
                    .description(lead.getDescription())
                    .carMake(lead.getCarMake())
                    .carModel(lead.getCarModel())
                    .carYear(lead.getCarYear())
                    .address(lead.getAddress())
                    .status(lead.getStatus().name())
                    .createdAt(lead.getCreatedAt())
                    .expiresAt(lead.getExpiresAt())
                    .imageUrls(imageUrls)
                    .build();
        }).collect(Collectors.toList());
    }

    public LeadResponseDto getLeadById(Long leadId, Long requestingUserId) {

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        var requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = lead.getCarOwner().getId().equals(requestingUserId);
        boolean isAdmin = requestingUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Unauthorized");
        }

        List<String> imageUrls = leadImageRepository.findByLeadId(lead.getId())
                .stream()
                .map(image -> s3Service.generateDownloadPresignedUrl(image.getImageUrl()))
                .collect(Collectors.toList());

        return LeadResponseDto.builder()
                .id(lead.getId())
                .title(lead.getTitle())
                .description(lead.getDescription())
                .carMake(lead.getCarMake())
                .carModel(lead.getCarModel())
                .carYear(lead.getCarYear())
                .address(lead.getAddress())
                .status(lead.getStatus().name())
                .createdAt(lead.getCreatedAt())
                .expiresAt(lead.getExpiresAt())
                .imageUrls(imageUrls)
                .build();
    }

    @Transactional
    public LeadResponseDto cancelLead(Long leadId, Long carOwnerId) {

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (!lead.getCarOwner().getId().equals(carOwnerId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (lead.getStatus() != LeadStatus.OPEN) {
            throw new RuntimeException("Only open leads can be cancelled");
        }

        lead.setStatus(LeadStatus.CANCELLED);
        lead = leadRepository.save(lead);

        List<String> imageUrls = leadImageRepository.findByLeadId(lead.getId())
                .stream()
                .map(image -> s3Service.generateDownloadPresignedUrl(image.getImageUrl()))
                .collect(Collectors.toList());

        activityLogService.log(lead.getCarOwner().getId(), ActivityType.LEAD_CANCELLED,
                "Cancelled lead: " + lead.getTitle(), lead.getId());

        return LeadResponseDto.builder()
                .id(lead.getId())
                .title(lead.getTitle())
                .description(lead.getDescription())
                .carMake(lead.getCarMake())
                .carModel(lead.getCarModel())
                .carYear(lead.getCarYear())
                .address(lead.getAddress())
                .status(lead.getStatus().name())
                .createdAt(lead.getCreatedAt())
                .expiresAt(lead.getExpiresAt())
                .imageUrls(imageUrls)
                .build();
    }

    public Page<AdminLeadResponseDto> getAllLeadsForAdmin(
            int page, int size, String status, String search) {

        PageRequest pageRequest = PageRequest.of(page, size);

        LeadStatus leadStatus = (status == null || status.isEmpty())
                ? null
                : LeadStatus.valueOf(status.toUpperCase());

        String trimmedSearch = (search == null || search.isBlank())
                ? null
                : search.trim();

        Page<Lead> leads = leadRepository.findAllWithFilters(
                leadStatus, trimmedSearch, pageRequest
        );

        return leads.map(lead -> AdminLeadResponseDto.builder()
                .id(lead.getId())
                .title(lead.getTitle())
                .carMake(lead.getCarMake())
                .carModel(lead.getCarModel())
                .carYear(lead.getCarYear())
                .address(lead.getAddress())
                .status(lead.getStatus().name())
                .ownerName(lead.getCarOwner().getFullName())
                .ownerEmail(lead.getCarOwner().getEmail())
                .createdAt(lead.getCreatedAt())
                .expiresAt(lead.getExpiresAt())
                .imageCount((int) leadImageRepository.countByLeadId(lead.getId()))
                .build());
    }
}