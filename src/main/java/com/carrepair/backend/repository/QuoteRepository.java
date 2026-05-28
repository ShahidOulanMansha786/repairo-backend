package com.carrepair.backend.repository;



import com.carrepair.backend.entity.Quote;
import com.carrepair.backend.entity.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByLeadId(Long leadId);

    List<Quote> findByRepairShopId(Long repairShopId);

    Optional<Quote> findByLeadIdAndRepairShopId(Long leadId, Long repairShopId);

    boolean existsByLeadIdAndRepairShopId(Long leadId, Long repairShopId);

//    List<Quote> findByLeadIdAndStatus(Long leadId, QuoteStatus status);

    List<Quote> findByLeadIdOrderByCreatedAtDesc(Long leadId);

    Optional<Quote> findByLeadIdAndStatus(Long leadId, QuoteStatus status);

    long countByRepairShopUserId(Long userId);

    @Query(value = """
    SELECT COUNT(*) FROM quotes
    WHERE created_at >= :start AND created_at < :end
    """, nativeQuery = true)
    long countByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
