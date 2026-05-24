package com.carrepair.backend.dto.response.quote;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptQuoteResponseDto {
    private Long quoteId;
    private Long leadId;
    private String status;
    private String channelId;
}
