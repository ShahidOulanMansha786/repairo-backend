package com.carrepair.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_owner_id", nullable = false)
    private User carOwner;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "car_make", nullable = false, length = 100)
    private String carMake;

    @Column(name = "car_model", nullable = false, length = 100)
    private String carModel;

    @Column(name = "car_year", nullable = false)
    private Integer carYear;

    @Column(name = "address", nullable = false)
    private String address;

    @JsonIgnore
    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeadStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(name = "shop_marked_done", nullable = false)
    private Boolean shopMarkedDone = false;

    @Builder.Default
    @Column(name = "owner_marked_satisfied", nullable = false)
    private Boolean ownerMarkedSatisfied = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "in_progress_at")
    private LocalDateTime inProgressAt;

    @JsonIgnore
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<LeadImage> images;

    @JsonIgnore
    @OneToMany(mappedBy = "lead")
    private List<Quote> quotes;
}
