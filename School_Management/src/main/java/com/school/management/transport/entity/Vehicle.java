package com.school.management.transport.entity;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;
}
