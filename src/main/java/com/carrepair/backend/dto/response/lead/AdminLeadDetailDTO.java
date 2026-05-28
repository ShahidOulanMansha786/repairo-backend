package com.carrepair.backend.dto.response.lead;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminLeadDetailDTO {

    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    private String carMake;
    private String carModel;
    private Integer carYear;

    private String address;

    private String customerName;

    private List<String> imageUrls;

    private AcceptedQuoteDTO acceptedQuote;

    @Data
    @Builder
    public static class AcceptedQuoteDTO {
        private Long quoteId;
        private BigDecimal price;
        private String message;
        private String shopName;
    }
}
