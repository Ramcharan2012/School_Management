package com.school.management.marks.entity;

import com.school.management.common.entity.BaseEntity;
import com.school.management.student.entity.Student;
import com.school.management.teacher.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;

/**
 * Stores a student's score for a specific exam.
 * Unique constraint: one mark entry per student per exam.
 */
@Entity
@Table(name = "marks", uniqueConstraints = @UniqueConstraint(columnNames = { "student_id",
        "exam_id" }, name = "uk_mark_student_exam"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mark extends BaseEntity {

    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;

    @Column(name = "grade")
    private String grade; // e.g., "A+", "B", "C", "F" — calculated by service

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_absent")
    @Builder.Default
    private Boolean isAbsent = false; // true if student was absent for this exam

    // ── Relationships ──────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by", nullable = false)
    private Teacher enteredBy;
}
