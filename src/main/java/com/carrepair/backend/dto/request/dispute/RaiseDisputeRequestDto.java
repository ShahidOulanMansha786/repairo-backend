package com.carrepair.backend.dto.request.dispute;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class RaiseDisputeRequestDto {
    private String reason;
}
