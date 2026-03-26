package com.school.management.academic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class/grade with a section (e.g., Grade 10 - Section A).
 * A ClassTeacher (Teacher) is assigned per class-section.
 */
@Entity
@Table(name = "class_grades", uniqueConstraints = @UniqueConstraint(columnNames = { "grade_name", "section",
        "academic_year_id" }, name = "uk_class_section_year"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassGrade extends BaseEntity {

    @NotBlank
    @Column(name = "grade_name", nullable = false)
    private String gradeName; // e.g., "Grade 10", "Class 5"

    @NotBlank
    @Column(name = "section", nullable = false)
    private String section; // e.g., "A", "B", "C"

    @Min(1)
    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "room_number")
    private String roomNumber;

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private com.school.management.teacher.entity.Teacher classTeacher;

    @JsonIgnore
    @OneToMany(mappedBy = "classGrade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.school.management.student.entity.Student> students = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "classGrade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubjectAssignment> subjectAssignments = new ArrayList<>();

    // ── Derived helper ────────────────────────────────────────────────────────
    @Transient
    public String getDisplayName() {
        return gradeName + " - Section " + section;
    }
}
