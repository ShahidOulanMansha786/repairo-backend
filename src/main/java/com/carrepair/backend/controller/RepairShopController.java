package com.carrepair.backend.controller;


import com.carrepair.backend.dto.request.repairshop.ShopApprovalRequestDto;
import com.carrepair.backend.dto.request.repairshop.ShopDocumentUploadDto;
import com.carrepair.backend.dto.response.lead.NearbyLeadResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopApprovalResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopDetailDto;
import com.carrepair.backend.dto.response.repairshop.ShopStatusResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopSummaryDto;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.RepairShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class RepairShopController {

    private final RepairShopService repairShopService;
    private final UserRepository userRepository;

    @PreAuthorize("hasAuthority('SHOP_OWNER')")
    @PostMapping("/documents")
    public ResponseEntity<ShopStatusResponseDto> uploadDocuments(@RequestBody ShopDocumentUploadDto dto) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(repairShopService.uploadDocuments(user.getId(), dto));
    }

    @PreAuthorize("hasAuthority('SHOP_OWNER')")
    @GetMapping("/my-status")
    public ResponseEntity<ShopStatusResponseDto> getMyStatus() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(repairShopService.getShopStatusByUserId(user.getId()));
    }

    @GetMapping("/leads/nearby")
    @PreAuthorize("hasAuthority('SHOP_OWNER')")
    public ResponseEntity<List<NearbyLeadResponseDto>> getNearbyLeads() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<NearbyLeadResponseDto> leads =
                repairShopService.getNearbyLeads(user.getId());

        return ResponseEntity.ok(leads);
    }


}