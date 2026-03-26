package com.school.management.academic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an academic department (e.g., Science, Mathematics, Arts).
 * A department can have multiple subjects and teachers.
 */
@Entity
@Table(name = "departments", uniqueConstraints = @UniqueConstraint(columnNames = "code", name = "uk_department_code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();
}
