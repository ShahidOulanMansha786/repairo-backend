package com.carrepair.backend.dto.response.lead;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLeadResponseDto {
    private Long id;
    private String title;
    private String carMake;
    private String carModel;
    private Integer carYear;
    private String address;
    private String status;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer imageCount;
}
