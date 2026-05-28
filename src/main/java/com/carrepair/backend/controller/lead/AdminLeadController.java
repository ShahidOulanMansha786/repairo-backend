package com.carrepair.backend.controller.lead;


import com.carrepair.backend.dto.response.lead.AdminLeadDetailDTO;
import com.carrepair.backend.dto.response.lead.AdminLeadResponseDto;
import com.carrepair.backend.service.lead.AdminLeadService;
import com.carrepair.backend.service.lead.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/leads")
@RequiredArgsConstructor
public class AdminLeadController {

    private final LeadService leadService;
    private final AdminLeadService adminLeadService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<AdminLeadResponseDto>> getAllLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {  // ← search add karo

        return ResponseEntity.ok(leadService.getAllLeadsForAdmin(page, size, status, search));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AdminLeadDetailDTO> getLeadDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminLeadService.getLeadById(id));
    }


}
