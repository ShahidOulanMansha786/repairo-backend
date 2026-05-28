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
import java.util.Optional;

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


    @Query("""
    SELECT l FROM Lead l
    JOIN l.carOwner u
    WHERE
      (:status IS NULL OR l.status = :status)
      AND (
        :search IS NULL OR :search = ''
        OR LOWER(l.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(l.carMake) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(l.carModel) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(CONCAT(l.carMake, ' ', l.carModel))
               LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
      )
    ORDER BY l.createdAt DESC
""")
    Page<Lead> findAllWithFilters(
            @Param("status") LeadStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    Long countByCarOwnerId(Long id);

    @Query("""
    SELECT l FROM Lead l
    LEFT JOIN FETCH l.images
    WHERE l.id = :id
""")
    Optional<Lead> findByIdWithImages(@Param("id") Long id);

    // Stats: count by date range
    @Query(value = """
    SELECT COUNT(*) FROM leads
    WHERE created_at >= :start AND created_at < :end
    """, nativeQuery = true)
    long countByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Stats: count by status and date range
    @Query(value = """
    SELECT COUNT(*) FROM leads
    WHERE status = :status
    AND created_at >= :start AND created_at < :end
    """, nativeQuery = true)
    long countByStatusAndDateRange(
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Trend: grouped by day
    @Query(value = """
    SELECT TO_CHAR(DATE_TRUNC('day', created_at), 'YYYY-MM-DD') as date,
           COUNT(*) as count
    FROM leads
    WHERE created_at >= :start AND created_at < :end
    GROUP BY DATE_TRUNC('day', created_at)
    ORDER BY DATE_TRUNC('day', created_at)
    """, nativeQuery = true)
    List<Object[]> countGroupedByDay(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Trend: grouped by month
    @Query(value = """
    SELECT TO_CHAR(DATE_TRUNC('month', created_at), 'YYYY-MM') as date,
           COUNT(*) as count
    FROM leads
    WHERE created_at >= :start AND created_at < :end
    GROUP BY DATE_TRUNC('month', created_at)
    ORDER BY DATE_TRUNC('month', created_at)
    """, nativeQuery = true)
    List<Object[]> countGroupedByMonth(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
