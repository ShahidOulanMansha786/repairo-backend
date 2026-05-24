package com.carrepair.backend.dto.response.lead;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyLeadResponseDto {
    private Long id;
    private String title;
    private String description;
    private String carMake;
    private String carModel;
    private Integer carYear;
    private String address;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<String> imageUrls;
    private Double distanceMeters;
    private Boolean hasQuoted;
}