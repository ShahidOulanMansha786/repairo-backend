package com.carrepair.backend.dto.response.dispute;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class DisputeResponseDto {
    private Long id;
    private Long leadId;
    private String raisedByName;
    private String reason;
    private String status;
    private String adminNote;
    private String createdAt;
    private String resolvedAt;
}
