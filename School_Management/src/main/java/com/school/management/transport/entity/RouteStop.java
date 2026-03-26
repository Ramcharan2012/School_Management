package com.school.management.transport.entity;

import com.school.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

/**
 * A single stop on a Route, with GPS coordinates for geofencing.
 * Stops are ordered by stopOrder within a Route.
 */
@Entity
@Table(name = "route_stops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStop extends BaseEntity {

    @NotBlank
    @Column(name = "stop_name", nullable = false)
    private String stopName; // e.g., "Main Street", "Park Avenue"

    @NotNull
    @Column(name = "latitude", nullable = false)
    private Double latitude;  // e.g., 13.0827

    @NotNull
    @Column(name = "longitude", nullable = false)
    private Double longitude; // e.g., 80.2707

    @Column(name = "stop_order")
    private Integer stopOrder; // 1, 2, 3... sequence on the route

    @Column(name = "pickup_time")
    private LocalTime pickupTime; // e.g., 07:30

    @Column(name = "drop_time")
    private LocalTime dropTime;   // e.g., 16:00

    // ── Relationship ──────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
}
