package com.school.management.academic.entity;

import com.school.management.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * The bridge/junction entity that assigns a Teacher to teach a specific Subject
 * in a specific ClassGrade for a specific AcademicYear.
 *
 * Example: "Mr. Sharma teaches Mathematics in Grade 10-A for 2024-2025"
 */
@Entity
@Table(name = "subject_assignments", uniqueConstraints = @UniqueConstraint(columnNames = { "teacher_id", "subject_id",
        "class_grade_id" }, name = "uk_subject_assignment"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectAssignment extends BaseEntity {

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private com.school.management.teacher.entity.Teacher teacher;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_grade_id", nullable = false)
    private ClassGrade classGrade;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
