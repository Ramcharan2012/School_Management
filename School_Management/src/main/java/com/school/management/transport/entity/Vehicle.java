package com.school.management.transport.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Represents a school bus/vehicle.
 * Each vehicle is assigned to a Route and driven by a driver.
 */
@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @NotBlank
    @Column(name = "vehicle_number", nullable = false, unique = true)
    private String vehicleNumber; // e.g., "KA-01-AB-1234"

    @Column(name = "vehicle_type")
    @Builder.Default
    private String vehicleType = "BUS"; // BUS, VAN, MINI_BUS

    @Column(name = "capacity")
    private Integer capacity; // e.g., 40

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone")
    private String driverPhone;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ── Relationship ──────────────────────────────────────────────────────
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    // ── JSON helpers ───────────────────────────────────────────────────
    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonProperty("route")
    public java.util.Map<String, Object> fetchRoute() {
        if (route == null) return null;
        return java.util.Map.of("id", route.getId(), "routeName", route.getRouteName());
    }
}
