package com.carrepair.backend.dto.response.dispute;

import com.carrepair.backend.enums.DisputeStatus;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeSummaryDTO {
    private Long id;
    private Long leadId;
    private Long raisedBy;
    private String reason;
    private DisputeStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String leadTitle;
    private String carMake;
    private String carModel;
    private String carOwnerName;
    private String carOwnerEmail;
    private String shopName;
    private String shopEmail;
}
