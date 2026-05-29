package com.carrepair.backend.repository;

import com.carrepair.backend.entity.Role;
import com.carrepair.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    @Query("""
    SELECT u FROM User u
    WHERE (:role IS NULL OR CAST(u.role AS string) = :role)
    AND (
        CAST(:status AS string) IS NULL OR
        (CAST(:status AS string) = 'blocked' AND u.isBlocked = true) OR
        (CAST(:status AS string) = 'active' AND u.isActive = true AND u.isBlocked = false) OR
        (CAST(:status AS string) = 'inactive' AND u.isActive = false)
    )
    AND (
        CAST(:search AS string) IS NULL OR
        LOWER(u.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
        LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
    )
    """)
    Page<User> findAllWithFilters(
            @Param("role") String role,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);

    long countByRole(Role role);

    List<User> findAllByRole(Role role);
}
