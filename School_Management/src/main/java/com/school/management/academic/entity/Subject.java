package com.school.management.academic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a school subject (e.g., Mathematics, Physics, English).
 * Belongs to a Department. Can be taught across multiple class grades by
 * different teachers.
 */
@Entity
@Table(name = "subjects", uniqueConstraints = @UniqueConstraint(columnNames = "code", name = "uk_subject_code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "code", nullable = false, unique = true)
    private String code; // e.g., "MATH101", "PHY201"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "credit_hours")
    private Integer creditHours;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @JsonIgnore
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubjectAssignment> subjectAssignments = new ArrayList<>();
}
