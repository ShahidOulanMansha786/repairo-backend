package com.carrepair.backend.repository;


import java.time.LocalDateTime;

public interface LeadDistanceProjection {
    Long getId();
    String getTitle();
    String getDescription();
    String getCarMake();
    String getCarModel();
    Integer getCarYear();
    String getAddress();
    String getStatus();
    LocalDateTime getCreatedAt();
    LocalDateTime getExpiresAt();
    Double getDistanceMeters();
}