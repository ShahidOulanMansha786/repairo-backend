package com.carrepair.backend.service.lead;

import com.carrepair.backend.dto.response.lead.AdminLeadDTO;
import com.carrepair.backend.dto.response.lead.AdminLeadDetailDTO;
import com.carrepair.backend.entity.Lead;
import com.carrepair.backend.entity.LeadStatus;
import com.carrepair.backend.entity.Quote;
import com.carrepair.backend.entity.QuoteStatus;
import com.carrepair.backend.repository.LeadRepository;
import com.carrepair.backend.repository.QuoteRepository;
import com.carrepair.backend.service.S3Service;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminLeadService {

    private final LeadRepository leadRepository;
    private final S3Service s3Service;
    private final QuoteRepository quoteRepository;

    public Page<AdminLeadDTO> getLeads(String search, String status, int page) {
        LeadStatus leadStatus = null;
        if (status != null && !status.isBlank()) {
            leadStatus = LeadStatus.valueOf(status.toUpperCase());
        }

        String trimmedSearch = (search != null && search.isBlank()) ? null : search;

        Pageable pageable = PageRequest.of(page, 10);

        return leadRepository
                .findAllWithFilters(leadStatus, trimmedSearch, pageable)
                .map(this::toDTO);
    }

    private AdminLeadDTO toDTO(Lead lead) {
        String car = lead.getCarMake() + " " + lead.getCarModel() + " " + lead.getCarYear();

        return AdminLeadDTO.builder()
                .id(lead.getId())
                .carOwnerId(lead.getCarOwner().getId())
                .car(car.trim())
                .issueDescription(lead.getDescription())
                .address(lead.getAddress())
                .status(lead.getStatus().name())
                .createdAt(lead.getCreatedAt())
                .build();
    }

    public byte[] exportLeadsCsv() {
        List<Lead> leads = leadRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Car Owner ID,Car,Issue Description,Address,Status,Created At\n");

        for (Lead lead : leads) {
            String car = lead.getCarMake() + " " + lead.getCarModel() + " " + lead.getCarYear();
            String desc = lead.getDescription() == null ? "" :
                    lead.getDescription().replace(",", " ").replace("\n", " ");

            csv.append(lead.getId()).append(",")
                    .append(lead.getCarOwner().getId()).append(",")
                    .append(car.trim()).append(",")
                    .append(desc).append(",")
                    .append(lead.getAddress() == null ? "" : lead.getAddress()).append(",")
                    .append(lead.getStatus().name()).append(",")
                    .append(lead.getCreatedAt()).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public AdminLeadDetailDTO getLeadById(Long id) {

        Lead lead = leadRepository.findByIdWithImages(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        // Presigned image URLs
        List<String> imageUrls = lead.getImages().stream()
                .map(img -> s3Service.generateDownloadPresignedUrl(img.getImageUrl()))
                .toList();

        AdminLeadDetailDTO.AcceptedQuoteDTO acceptedQuoteDTO = null;

        Optional<Quote> acceptedQuote = quoteRepository
                .findByLeadIdAndStatus(lead.getId(), QuoteStatus.ACCEPTED);

        if (acceptedQuote.isPresent()) {
            Quote q = acceptedQuote.get();
            acceptedQuoteDTO = AdminLeadDetailDTO.AcceptedQuoteDTO.builder()
                    .quoteId(q.getId())
                    .price(q.getPrice())
                    .message(q.getMessage())
                    .shopName(q.getRepairShop().getShopName())
                    .build();
        }

        String customerName = lead.getCarOwner().getFullName();

        return AdminLeadDetailDTO.builder()
                .id(lead.getId())
                .title(lead.getTitle())
                .description(lead.getDescription())
                .status(lead.getStatus().name())
                .createdAt(lead.getCreatedAt())
                .carMake(lead.getCarMake())
                .carModel(lead.getCarModel())
                .carYear(lead.getCarYear())
                .address(lead.getAddress())
                .customerName(customerName)
                .imageUrls(imageUrls)
                .acceptedQuote(acceptedQuoteDTO)
                .build();
    }
}
