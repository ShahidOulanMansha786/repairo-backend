package com.carrepair.backend.repository;



import com.carrepair.backend.entity.RepairShop;
import com.carrepair.backend.service.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairShopRepository extends JpaRepository<RepairShop, Long> {

    Optional<RepairShop> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query(
            value = """
        SELECT rs.*
        FROM repair_shops rs
        WHERE ST_DWithin(
            rs.location::geography,
            ST_MakePoint(:longitude, :latitude)::geography,
            :distanceInMeters
        )
        AND rs.is_verified = true
        AND rs.is_active = true
    """,
            nativeQuery = true
    )
    List<RepairShop> findVerifiedShopsWithinDistance(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("distanceInMeters") double distanceInMeters
    );

    Page<RepairShop> findAllByApprovalStatus(ApprovalStatus status, Pageable pageable);

    Page<RepairShop> findAll(Pageable pageable);
}
