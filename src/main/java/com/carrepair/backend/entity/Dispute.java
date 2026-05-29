package com.carrepair.backend.entity;

import com.carrepair.backend.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_id", nullable = false, unique = true)
    private Long leadId;

    @Column(name = "raised_by", nullable = false)
    private Long raisedBy;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status;

    @Column(name = "admin_note")
    private String adminNote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
