package com.carrepair.backend.repository;



import com.carrepair.backend.entity.RepairShop;
import com.carrepair.backend.service.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
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

    @Query("SELECT r FROM RepairShop r JOIN r.user u WHERE " +
            "(:status IS NULL OR r.approvalStatus = :status) AND " +
            "(:search IS NULL OR " +
            "LOWER(r.shopName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RepairShop> findAllWithFilters(
            @Param("status") ApprovalStatus status,
            @Param("search") String search,
            Pageable pageable);

    long countByApprovalStatus(ApprovalStatus status);

    List<RepairShop> findTop5ByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus approvalStatus);

    @Query(value = """
    SELECT COUNT(*) FROM repair_shops
    WHERE approval_status = 'APPROVED'
    AND is_active = true
    """, nativeQuery = true)
    long countActiveShops();

    @Query(value = """
    SELECT rs.id,
           rs.shop_name,
           rs.address,
           rs.is_active,
           COUNT(q.id) as booking_count
    FROM repair_shops rs
    JOIN quotes q ON q.repair_shop_id = rs.id
    JOIN leads l ON l.id = q.lead_id
    WHERE q.status = 'ACCEPTED'
    AND l.created_at >= :start AND l.created_at < :end
    GROUP BY rs.id, rs.shop_name, rs.address, rs.is_active
    ORDER BY booking_count DESC
    LIMIT 5
    """, nativeQuery = true)
    List<Object[]> findTopShopsByAcceptedQuotes(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Approved shops in a date range (approved_at se compare)
    @Query(value = """
    SELECT COUNT(*) FROM repair_shops
    WHERE approval_status = 'APPROVED'
    AND is_active = true
    AND approved_at >= :start AND approved_at < :end
    """, nativeQuery = true)
    long countApprovedShopsByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}
