package com.carrepair.backend.dto.response.lead;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AdminLeadDTO {
    private Long id;
    private Long carOwnerId;
    private String car;           // "Toyota Camry 2021"
    private String issueDescription;
    private String address;
    private String status;
    private LocalDateTime createdAt;
}
