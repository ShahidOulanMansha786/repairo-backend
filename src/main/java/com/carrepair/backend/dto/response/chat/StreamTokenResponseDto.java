package com.carrepair.backend.dto.response.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamTokenResponseDto {
    private String streamToken;
    private String channelId;
}
