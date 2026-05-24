package com.carrepair.backend.dto.request.chat;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatNotifyRequestDto {
    private String channelId;
    private String senderName;
    private String messagePreview;
}