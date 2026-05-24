package com.carrepair.backend.service;


import com.carrepair.backend.dto.request.CreateCarOwnerRequest;
import com.carrepair.backend.dto.request.CreateRepairShopRequest;
import com.carrepair.backend.entity.RepairShop;
import com.carrepair.backend.entity.Role;
import com.carrepair.backend.entity.User;
import com.carrepair.backend.repository.RepairShopRepository;
import com.carrepair.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestService {

    private final UserRepository userRepository;
    private final RepairShopRepository repairShopRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public User createCarOwner(CreateCarOwnerRequest request) {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(Role.CAR_OWNER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    public RepairShop createRepairShop(CreateRepairShopRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Point location = geometryFactory.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude())
        );
        location.setSRID(4326);

        RepairShop shop = RepairShop.builder()
                .user(user)
                .shopName(request.getShopName())
                .description(request.getDescription())
                .phone(request.getPhone())
                .address(request.getAddress())
                .location(location)
                .cnicUrl(request.getCnicUrl())
                .businessDocUrl(request.getBusinessDocUrl())
                .isVerified(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        return repairShopRepository.save(shop);
    }

    public List<RepairShop> findNearbyShops(double latitude, double longitude) {
        return repairShopRepository.findVerifiedShopsWithinDistance(latitude, longitude, 25000);
    }
}