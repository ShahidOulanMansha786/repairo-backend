package com.carrepair.backend.repository;


import com.carrepair.backend.entity.LeadImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadImageRepository extends JpaRepository<LeadImage, Long> {

    List<LeadImage> findByLeadId(Long leadId);

    long countByLeadId(Long leadId);
}