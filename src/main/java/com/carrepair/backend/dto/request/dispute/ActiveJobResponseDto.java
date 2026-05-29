package com.carrepair.backend.dto.request.dispute;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ActiveJobResponseDto {
    private Long leadId;
    private Long quoteId;
    private String title;
    private String description;
    private String carMake;
    private String carModel;
    private Integer carYear;
    private String address;
    private String status;
    private Boolean shopMarkedDone;
    private Boolean ownerMarkedSatisfied;
    private String inProgressAt;
    private BigDecimal price;
    private List<String> imageUrls;
    private String carOwnerName;
}
