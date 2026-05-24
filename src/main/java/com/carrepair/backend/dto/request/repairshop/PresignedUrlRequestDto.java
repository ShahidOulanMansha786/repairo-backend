package com.carrepair.backend.dto.request.repairshop;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlRequestDto {
    private String folder;
    private String fileName;
    private String contentType;
}