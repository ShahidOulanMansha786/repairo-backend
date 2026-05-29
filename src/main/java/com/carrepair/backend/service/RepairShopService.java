package com.carrepair.backend.service;

import com.carrepair.backend.dto.request.dispute.ActiveJobResponseDto;
import com.carrepair.backend.dto.request.repairshop.ShopDocumentUploadDto;
import com.carrepair.backend.dto.response.lead.NearbyLeadResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopApprovalResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopDetailDto;
import com.carrepair.backend.dto.response.repairshop.ShopStatusResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopSummaryDto;
import com.carrepair.backend.entity.*;
import com.carrepair.backend.repository.*;
import com.carrepair.backend.service.fcm.FcmService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairShopService {

    private final RepairShopRepository repairShopRepository;
    private final FcmService fcmService;
    private final S3Service s3Service;
    private final LeadImageRepository leadImageRepository;
    private final LeadRepository leadRepository;
    private final QuoteRepository  quoteRepository;
    private final UserRepository userRepository;

    @Transactional
    public ShopStatusResponseDto uploadDocuments(Long userId, ShopDocumentUploadDto dto) {
        RepairShop shop = repairShopRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Repair shop not found"));

        if (shop.getApprovalStatus() != ApprovalStatus.INCOMPLETE) {
            throw new RuntimeException("Documents already submitted");
        }

        shop.setLogoUrl(dto.getLogoKey());
        shop.setCnicUrl(dto.getCnicKey());
        shop.setBusinessDocUrl(dto.getBusinessDocKey());
        shop.setApprovalStatus(ApprovalStatus.PENDING);

        repairShopRepository.save(shop);

        return ShopStatusResponseDto.builder()
                .shopName(shop.getShopName())
                .approvalStatus(shop.getApprovalStatus().name())
                .rejectionReason(shop.getRejectionReason())
                .build();
    }

    public ShopStatusResponseDto getShopStatusByUserId(Long userId) {
        RepairShop shop = repairShopRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return ShopStatusResponseDto.builder()
                .shopName(shop.getShopName())
                .approvalStatus(shop.getApprovalStatus().name())
                .rejectionReason(shop.getRejectionReason())
                .build();
    }

    @Transactional
    public ShopApprovalResponseDto approveShop(Long shopId) {
        RepairShop shop = repairShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        if (shop.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Shop is not in PENDING status");
        }

        shop.setApprovalStatus(ApprovalStatus.APPROVED);
        shop.setIsVerified(true);
        shop.setApprovedAt(LocalDateTime.now());
        repairShopRepository.save(shop);

        User user = shop.getUser();
        if (user.getFcmToken() != null) {
            fcmService.sendShopApprovedNotification(user.getFcmToken(), shop.getShopName());
        }

        return ShopApprovalResponseDto.builder()
                .shopId(shop.getId())
                .approvalStatus(ApprovalStatus.APPROVED.name())
                .message("Shop has been approved successfully")
                .build();
    }

    @Transactional
    public ShopApprovalResponseDto rejectShop(Long shopId, String reason) {
        RepairShop shop = repairShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        if (shop.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Shop is not in PENDING status");
        }

        shop.setApprovalStatus(ApprovalStatus.REJECTED);
        shop.setRejectionReason(reason);
        shop.setRejectedAt(LocalDateTime.now());
        repairShopRepository.save(shop);

        User user = shop.getUser();
        if (user.getFcmToken() != null) {
            fcmService.sendShopRejectedNotification(user.getFcmToken(), shop.getShopName(), reason);
        }

        return ShopApprovalResponseDto.builder()
                .shopId(shop.getId())
                .approvalStatus(ApprovalStatus.REJECTED.name())
                .message("Shop has been rejected")
                .build();
    }

    public Page<ShopSummaryDto> getPendingShops(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RepairShop> shops = repairShopRepository.findAllByApprovalStatus(ApprovalStatus.PENDING, pageable);
        return shops.map(shop -> ShopSummaryDto.builder()
                .shopId(shop.getId())
                .shopName(shop.getShopName())
                .ownerName(shop.getUser().getFullName())
                .ownerEmail(shop.getUser().getEmail())
                .ownerPhone(shop.getUser().getPhone())
                .address(shop.getAddress())
                .approvalStatus(shop.getApprovalStatus().name())
                .createdAt(shop.getCreatedAt())
                .build());
    }

    public Page<ShopSummaryDto> getAllShops(ApprovalStatus status, String search, int page, int size) {

        String searchParam = (search == null) ? "" : search.trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<RepairShop> shops = repairShopRepository.findAllWithFilters(status, searchParam, pageable);

        return shops.map(shop -> ShopSummaryDto.builder()
                .shopId(shop.getId())
                .shopName(shop.getShopName())
                .ownerName(shop.getUser().getFullName())
                .ownerEmail(shop.getUser().getEmail())
                .ownerPhone(shop.getUser().getPhone())
                .address(shop.getAddress())
                .approvalStatus(shop.getApprovalStatus().name())
                .createdAt(shop.getCreatedAt())
                .build());
    }


    public Map<String, Long> getShopStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("pending", repairShopRepository.countByApprovalStatus(ApprovalStatus.PENDING));
        counts.put("approved", repairShopRepository.countByApprovalStatus(ApprovalStatus.APPROVED));
        counts.put("rejected", repairShopRepository.countByApprovalStatus(ApprovalStatus.REJECTED));
        return counts;
    }

    public ShopDetailDto getShopDetail(Long shopId) {
        RepairShop shop = repairShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        String logoUrl = shop.getLogoUrl() != null
                ? s3Service.generateDownloadPresignedUrl(shop.getLogoUrl()) : null;
        String cnicUrl = shop.getCnicUrl() != null
                ? s3Service.generateDownloadPresignedUrl(shop.getCnicUrl()) : null;
        String businessDocUrl = shop.getBusinessDocUrl() != null
                ? s3Service.generateDownloadPresignedUrl(shop.getBusinessDocUrl()) : null;

        double latitude = shop.getLocation() != null
                ? shop.getLocation().getY() : 0.0;
        double longitude = shop.getLocation() != null
                ? shop.getLocation().getX() : 0.0;

        return ShopDetailDto.builder()
                .shopId(shop.getId())
                .shopName(shop.getShopName())
                .ownerName(shop.getUser().getFullName())
                .ownerEmail(shop.getUser().getEmail())
                .ownerPhone(shop.getUser().getPhone())
                .address(shop.getAddress())
                .latitude(latitude)
                .longitude(longitude)
                .approvalStatus(shop.getApprovalStatus().name())
                .rejectionReason(shop.getRejectionReason())
                .createdAt(shop.getCreatedAt())
                .logoUrl(logoUrl)
                .cnicUrl(cnicUrl)
                .businessDocUrl(businessDocUrl)
                .build();
    }

    public List<NearbyLeadResponseDto> getNearbyLeads(Long userId) {
        RepairShop shop = repairShopRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        if (shop.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new RuntimeException("Shop not approved");
        }

        Point location = shop.getLocation();
        Double latitude = location.getY();
        Double longitude = location.getX();

        List<LeadDistanceProjection> leads =
                leadRepository.findOpenLeadsWithinDistance(latitude, longitude, 25000.0);

        return leads.stream().map(lead -> {
            List<String> imageUrls = leadImageRepository.findByLeadId(lead.getId())
                    .stream()
                    .map(img -> s3Service.generateDownloadPresignedUrl(img.getImageUrl()))
                    .collect(Collectors.toList());

            Boolean hasQuoted = quoteRepository.existsByLeadIdAndRepairShopId(
                    lead.getId(), shop.getId());

            return NearbyLeadResponseDto.builder()
                    .id(lead.getId())
                    .title(lead.getTitle())
                    .description(lead.getDescription())
                    .carMake(lead.getCarMake())
                    .carModel(lead.getCarModel())
                    .carYear(lead.getCarYear())
                    .address(lead.getAddress())
                    .status(lead.getStatus())
                    .createdAt(lead.getCreatedAt())
                    .expiresAt(lead.getExpiresAt())
                    .imageUrls(imageUrls)
                    .distanceMeters(lead.getDistanceMeters())
                    .hasQuoted(hasQuoted)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<ActiveJobResponseDto> getMyActiveJobs(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RepairShop shop = repairShopRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Repair shop not found"));

        List<Quote> acceptedQuotes = quoteRepository
                .findAllByRepairShopIdAndStatus(shop.getId(), QuoteStatus.ACCEPTED);

        return acceptedQuotes.stream().map(quote -> {
            Lead lead = leadRepository.findById(quote.getLead().getId())
                    .orElseThrow(() -> new RuntimeException("Lead not found"));

            User carOwner = userRepository.findById(lead.getCarOwner().getId())
                    .orElseThrow(() -> new RuntimeException("Car owner not found"));

            List<String> imageUrls = leadImageRepository
                    .findByLeadId(lead.getId())
                    .stream()
                    .map(img -> s3Service.generateDownloadPresignedUrl(img.getImageUrl()))
                    .collect(Collectors.toList());

            return ActiveJobResponseDto.builder()
                    .leadId(lead.getId())
                    .quoteId(quote.getId())
                    .title(lead.getTitle())
                    .description(lead.getDescription())
                    .carMake(lead.getCarMake())
                    .carModel(lead.getCarModel())
                    .carYear(lead.getCarYear())
                    .address(lead.getAddress())
                    .status(lead.getStatus().name())
                    .shopMarkedDone(lead.getShopMarkedDone())
                    .ownerMarkedSatisfied(lead.getOwnerMarkedSatisfied())
                    .inProgressAt(lead.getInProgressAt() != null
                            ? lead.getInProgressAt().toString() : null)
                    .price(quote.getPrice())
                    .imageUrls(imageUrls)
                    .carOwnerName(carOwner.getFullName())
                    .build();

        }).collect(Collectors.toList());
    }
}