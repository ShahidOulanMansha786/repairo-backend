package com.carrepair.backend.repository;

import com.carrepair.backend.entity.Dispute;
import com.carrepair.backend.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository
        extends JpaRepository<Dispute, Long> {

    Optional<Dispute> findByLeadId(Long leadId);
    List<Dispute> findAllByStatus(DisputeStatus status);
    List<Dispute> findAllByOrderByCreatedAtDesc();
}
