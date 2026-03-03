package com.school.management.academic.entity;

import com.school.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a school academic year (e.g., 2024-2025).
 * Only one academic year can be active at a time.
 */
@Entity
@Table(name = "academic_years")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYear extends BaseEntity {

    @NotBlank
    @Column(name = "year_label", nullable = false)
    private String yearLabel; // e.g., "2024-2025"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    @Column(name = "description")
    private String description;

    // ── Relationships ──────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "academicYear", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ClassGrade> classGrades = new ArrayList<>();
}
