package com.carrepair.backend.controller;

import com.carrepair.backend.dto.request.repairshop.ShopApprovalRequestDto;
import com.carrepair.backend.dto.response.repairshop.ShopApprovalResponseDto;
import com.carrepair.backend.dto.response.repairshop.ShopDetailDto;
import com.carrepair.backend.dto.response.repairshop.ShopSummaryDto;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.AdminAuthService;
import com.carrepair.backend.service.RepairShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final RepairShopService repairShopService;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(repairShopService.getAllShops(page, size));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/shops/{shopId}")
    public ResponseEntity<ShopDetailDto> getShopDetail(@PathVariable Long shopId) {
        return ResponseEntity.ok(repairShopService.getShopDetail(shopId));
    }
}
