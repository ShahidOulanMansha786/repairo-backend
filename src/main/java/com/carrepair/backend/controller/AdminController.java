package com.carrepair.backend.controller;

import com.carrepair.backend.dto.request.repairshop.ShopApprovalRequestDto;
import com.carrepair.backend.dto.response.lead.AdminLeadDTO;
import com.carrepair.backend.dto.response.repairshop.ShopApprovalResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopDetailDto;
import com.carrepair.backend.dto.response.repairshop.ShopSummaryDto;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.AdminAuthService;
import com.carrepair.backend.service.ApprovalStatus;
import com.carrepair.backend.service.RepairShopService;
import com.carrepair.backend.service.lead.AdminLeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final RepairShopService repairShopService;
    private final AdminLeadService adminLeadService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/shops/{shopId}/approve")
    public ResponseEntity<ShopApprovalResponseDto> approveShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(repairShopService.approveShop(shopId));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/shops/{shopId}/reject")
    public ResponseEntity<ShopApprovalResponseDto> rejectShop(
            @PathVariable Long shopId,
            @RequestBody ShopApprovalRequestDto dto) {
        return ResponseEntity.ok(repairShopService.rejectShop(shopId, dto.getRejectionReason()));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/shops/pending")
    public ResponseEntity<Page<ShopSummaryDto>> getPendingShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(repairShopService.getPendingShops(page, size));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/shops")
    public ResponseEntity<Page<ShopSummaryDto>> getAllShops(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApprovalStatus approvalStatus = null;
        if (status != null && !status.isBlank()) {
            approvalStatus = ApprovalStatus.valueOf(status.toUpperCase());
        }

        log.info("status:{}, search:{}, size:{}", status, search, size);

        return ResponseEntity.ok(
                repairShopService.getAllShops(approvalStatus, search, page, size));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/shops/counts")
    public ResponseEntity<Map<String, Long>> getShopCounts() {
        log.info("count controller has been called");
        return ResponseEntity.ok(repairShopService.getShopStatusCounts());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/shops/{shopId}")
    public ResponseEntity<ShopDetailDto> getShopDetail(@PathVariable Long shopId) {
        return ResponseEntity.ok(repairShopService.getShopDetail(shopId));
    }



    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/leads/export")
    public ResponseEntity<byte[]> exportLeads() {
        byte[] csvData = adminLeadService.exportLeadsCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"leads.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
