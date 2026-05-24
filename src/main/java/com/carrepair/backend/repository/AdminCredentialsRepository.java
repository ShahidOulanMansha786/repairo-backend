package com.carrepair.backend.repository;

import com.carrepair.backend.entity.AdminCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminCredentialsRepository extends JpaRepository<AdminCredentials, Long> {

    Optional<AdminCredentials> findByUserId(Long userId);
}
