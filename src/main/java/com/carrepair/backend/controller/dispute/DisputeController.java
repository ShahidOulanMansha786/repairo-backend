package com.carrepair.backend.controller.dispute;

import com.carrepair.backend.dto.request.dispute.AdminResolveDisputeRequestDto;
import com.carrepair.backend.dto.request.dispute.RaiseDisputeRequestDto;
import com.carrepair.backend.dto.response.dispute.DisputeResponseDto;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.dispute.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;
    private final UserRepository userRepository;

    @PostMapping("/leads/{leadId}/dispute")
    @PreAuthorize("hasAuthority('CAR_OWNER')")
    public ResponseEntity<DisputeResponseDto> raiseDispute(
            @PathVariable Long leadId,
            @RequestBody RaiseDisputeRequestDto dto) {

        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Long carOwnerId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disputeService.raiseDispute(leadId, carOwnerId, dto.getReason()));
    }

    @GetMapping("/leads/{leadId}/dispute")
    @PreAuthorize("hasAnyAuthority('CAR_OWNER', 'REPAIR_SHOP')")
    public ResponseEntity<DisputeResponseDto> getDisputeByLead(
            @PathVariable Long leadId) {

        return ResponseEntity.ok(disputeService.getDisputeByLead(leadId));
    }

    @GetMapping("/admin/disputes")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<DisputeResponseDto>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @PostMapping("/admin/disputes/{disputeId}/resolve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DisputeResponseDto> resolveDispute(
            @PathVariable Long disputeId,
            @RequestBody AdminResolveDisputeRequestDto dto) {

        return ResponseEntity.ok(
                disputeService.adminResolveDispute(disputeId, dto));
    }
}
