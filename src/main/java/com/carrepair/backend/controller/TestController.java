package com.carrepair.backend.controller;


import com.carrepair.backend.dto.request.CreateCarOwnerRequest;
import com.carrepair.backend.dto.request.CreateRepairShopRequest;
import com.carrepair.backend.entity.RepairShop;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.service.MailService;
import com.carrepair.backend.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;
    private final MailService mailService;

    @PostMapping("/car-owner")
    public ResponseEntity<User> createCarOwner(@RequestBody CreateCarOwnerRequest request) {
        User created = testService.createCarOwner(request);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/repair-shop")
    public ResponseEntity<RepairShop> createRepairShop(@RequestBody CreateRepairShopRequest request) {
        RepairShop created = testService.createRepairShop(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/nearby-shops")
    public ResponseEntity<List<RepairShop>> getNearbyShops(
            @RequestParam double lat,
            @RequestParam double lng) {
        List<RepairShop> shops = testService.findNearbyShops(lat, lng);
        return ResponseEntity.ok(shops);
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> testSendEmail() {
        mailService.sendOtpEmail("shahidoulan@gmail.com", "847291");
        return ResponseEntity.ok("Email sent");
    }
}