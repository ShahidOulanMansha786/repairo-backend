package com.carrepair.backend.controller;


import com.carrepair.backend.dto.UserCountsDto;
import com.carrepair.backend.dto.UserDetailDto;
import com.carrepair.backend.dto.UserListDto;
import com.carrepair.backend.dto.response.MessageResponseDto;
import com.carrepair.backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserListDto>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminUserService.getUsers(role, status, search, pageable));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/counts")
    public ResponseEntity<UserCountsDto> getCounts() {
        return ResponseEntity.ok(adminUserService.getCounts());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailDto> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserDetail(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/block")
    public ResponseEntity<MessageResponseDto> blockUser(@PathVariable Long id) {
        adminUserService.blockUser(id);
        return ResponseEntity.ok(new MessageResponseDto("User blocked successfully."));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/unblock")
    public ResponseEntity<MessageResponseDto> unblockUser(@PathVariable Long id) {
        adminUserService.unblockUser(id);
        return ResponseEntity.ok(new MessageResponseDto("User unblocked successfully."));
    }
}
