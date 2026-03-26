package com.school.management.transport.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A named route (e.g. "Morning Route A") consisting of ordered stops.
 */
@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route extends BaseEntity {

    @NotBlank
    @Column(name = "route_name", nullable = false, unique = true)
    private String routeName; // e.g., "Morning Route A"

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ── Relationships ─────────────────────────────────────────────────────
    @JsonIgnore
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("stopOrder ASC")
    @Builder.Default
    private List<RouteStop> stops = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vehicle> vehicles = new ArrayList<>();
}
