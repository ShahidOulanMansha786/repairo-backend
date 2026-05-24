package com.carrepair.backend.repository;



import com.carrepair.backend.entity.Lead;
import com.carrepair.backend.entity.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findByCarOwnerId(Long carOwnerId);

    List<Lead> findAllByStatus(LeadStatus status);

    List<Lead> findByCarOwnerIdAndStatus(Long carOwnerId, LeadStatus status);

    List<Lead> findByCarOwnerIdOrderByCreatedAtDesc(Long carOwnerId);

    List<Lead> findByStatusAndExpiresAtBefore(LeadStatus status, LocalDateTime time);

    Page<Lead> findAll(Pageable pageable);

    Page<Lead> findAllByStatus(LeadStatus status, Pageable pageable);

    @Query(value = """
    SELECT l.id,
           l.title,
           l.description,
           l.car_make as carMake,
           l.car_model as carModel,
           l.car_year as carYear,
           l.address,
           l.status,
           l.created_at as createdAt,
           l.expires_at as expiresAt,
           ST_Distance(
               l.location::geography,
               ST_MakePoint(:longitude, :latitude)::geography
           ) as distanceMeters
    FROM leads l
    WHERE l.status = 'OPEN'
    AND ST_DWithin(
        l.location::geography,
        ST_MakePoint(:longitude, :latitude)::geography,
        :distanceMeters
    )
    ORDER BY distanceMeters ASC
    """, nativeQuery = true)
    List<LeadDistanceProjection> findOpenLeadsWithinDistance(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("distanceMeters") Double distanceMeters);
}
