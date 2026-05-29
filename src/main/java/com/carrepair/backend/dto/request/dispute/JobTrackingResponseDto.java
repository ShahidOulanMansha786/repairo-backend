package com.carrepair.backend.dto.request.dispute;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class JobTrackingResponseDto {
    private Long leadId;
    private String status;
    private Boolean shopMarkedDone;
    private Boolean ownerMarkedSatisfied;
    private String inProgressAt;
    private String completedAt;
}
