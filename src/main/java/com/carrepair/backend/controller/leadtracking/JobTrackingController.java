package com.carrepair.backend.controller.leadtracking;

import com.carrepair.backend.dto.request.dispute.JobTrackingResponseDto;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.leadtracking.JobTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class JobTrackingController {

    private final JobTrackingService jobTrackingService;
    private final UserRepository userRepository;

    @PostMapping("/{leadId}/shop-done")
    @PreAuthorize("hasAuthority('SHOP_OWNER')")
    public ResponseEntity<JobTrackingResponseDto> shopMarksDone(
            @PathVariable Long leadId) {

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return ResponseEntity.ok(
                jobTrackingService.shopMarksDone(leadId, userId));
    }

    @PostMapping("/{leadId}/owner-satisfied")
    @PreAuthorize("hasAuthority('CAR_OWNER')")
    public ResponseEntity<JobTrackingResponseDto> ownerMarksSatisfied(
            @PathVariable Long leadId) {

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return ResponseEntity.ok(
                jobTrackingService.ownerMarksSatisfied(leadId, userId));
    }

    @GetMapping("/{leadId}/job-status")
    @PreAuthorize("hasAnyAuthority('CAR_OWNER', 'SHOP_OWNER')")
    public ResponseEntity<JobTrackingResponseDto> getJobStatus(
            @PathVariable Long leadId) {

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return ResponseEntity.ok(
                jobTrackingService.getJobStatus(leadId, userId));
    }
}
