package com.carrepair.backend.repository;



import com.carrepair.backend.entity.Quote;
import com.carrepair.backend.entity.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
