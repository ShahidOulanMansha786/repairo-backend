package com.carrepair.backend.controller.lead;


import com.carrepair.backend.dto.request.lead.CreateLeadRequestDto;
import com.carrepair.backend.dto.response.lead.LeadResponseDto;
import com.carrepair.backend.repository.UserRepository;
import com.carrepair.backend.service.lead.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('CAR_OWNER')")
    public ResponseEntity<LeadResponseDto> createLead(@RequestBody CreateLeadRequestDto dto) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        LeadResponseDto response = leadService.createLead(userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('CAR_OWNER')")
    public ResponseEntity<List<LeadResponseDto>> getMyLeads() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return ResponseEntity.ok(leadService.getMyLeads(userId));
    }

    @GetMapping("/{leadId}")
    @PreAuthorize("hasAuthority('CAR_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<LeadResponseDto> getLeadById(@PathVariable Long leadId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return ResponseEntity.ok(leadService.getLeadById(leadId, userId));
    }

    @PatchMapping("/{leadId}/cancel")
    @PreAuthorize("hasAuthority('CAR_OWNER')")
    public ResponseEntity<LeadResponseDto> cancelLead(@PathVariable Long leadId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return ResponseEntity.ok(leadService.cancelLead(leadId, userId));
    }
}
