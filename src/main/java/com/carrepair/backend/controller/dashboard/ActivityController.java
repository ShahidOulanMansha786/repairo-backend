package com.carrepair.backend.controller.dashboard;


import com.carrepair.backend.dto.response.dashboard.ActivityLogResponse;
import com.carrepair.backend.service.dashboard.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/activity")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/recent")
    public ResponseEntity<List<ActivityLogResponse>>
    getRecent() {
        return ResponseEntity.ok(
                activityService.getRecentActivity());
    }

    @GetMapping
    public ResponseEntity<Page<ActivityLogResponse>> getAll(
            @RequestParam(required = false) String type,
            Pageable pageable) {
        return ResponseEntity.ok(
                activityService.getAllActivity(type, pageable));
    }
}
