package com.carrepair.backend.dto.request.quote;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuoteRequestDto {
    private Long leadId;
    private BigDecimal price;
    private String message;
}