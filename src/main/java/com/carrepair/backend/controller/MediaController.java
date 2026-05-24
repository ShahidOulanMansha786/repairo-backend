package com.carrepair.backend.controller;


import com.carrepair.backend.dto.request.repairshop.PresignedUrlRequestDto;
import com.carrepair.backend.dto.response.repairshop.PresignedUrlResponseDto;
import com.carrepair.backend.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final S3Service s3Service;

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponseDto> getPresignedUrl(
            @RequestBody PresignedUrlRequestDto dto) {

        String objectKey = dto.getFolder() + "/" +
                UUID.randomUUID() + "_" + dto.getFileName();

        String uploadUrl = s3Service.generateUploadPresignedUrl(
                objectKey,
                dto.getContentType()
        );

        return ResponseEntity.ok(
                PresignedUrlResponseDto.builder()
                        .uploadUrl(uploadUrl)
                        .objectKey(objectKey)
                        .build()
        );
    }
}