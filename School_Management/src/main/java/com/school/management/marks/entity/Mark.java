package com.school.management.marks.entity;

import com.school.management.common.entity.BaseEntity;
import com.school.management.student.entity.Student;
import com.school.management.teacher.entity.Teacher;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import lombok.*;

/**
 * Stores a student's score for a specific exam.
 * Unique constraint: one mark entry per student per exam.
 */
@Audited
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

    @JsonIgnore
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @JsonIgnore
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @JsonIgnore
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by", nullable = false)
    private Teacher enteredBy;

    // ── JSON helpers ──────────────────────────────────────────────────────────
    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("student")
    public java.util.Map<String, Object> fetchStudent() {
        if (student == null) return null;
        String fn = student.getUser() != null ? student.getUser().getFirstName() : "";
        String ln = student.getUser() != null ? student.getUser().getLastName() : "";
        return java.util.Map.of("id", student.getId(), "rollNumber", student.getRollNumber() != null ? student.getRollNumber() : "",
                "user", java.util.Map.of("firstName", fn, "lastName", ln));
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("exam")
    public java.util.Map<String, Object> fetchExam() {
        if (exam == null) return null;
        return java.util.Map.of("id", exam.getId(), "title", exam.getTitle(),
                "totalMarks", exam.getTotalMarks(), "passingMarks", exam.getPassingMarks(),
                "examType", exam.getExamType() != null ? exam.getExamType().name() : "",
                "isPublished", exam.getIsPublished());
    }
}
