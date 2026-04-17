package com.school.management.attendance.entity;

import com.school.management.academic.entity.Subject;
import com.school.management.common.entity.BaseEntity;
import com.school.management.common.enums.AttendanceStatus;
import com.school.management.student.entity.Student;
import com.school.management.teacher.entity.Teacher;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Records a student's attendance for a specific subject on a specific date.
 * Unique constraint ensures one attendance record per student per subject per
 * date.
 * Only Teachers (or Admin) can mark attendance.
 */
@Entity
@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(columnNames = { "student_id", "subject_id",
        "attendance_date" }, name = "uk_attendance_student_subject_date"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Column(name = "remarks")
    private String remarks;

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by", nullable = false)
    private Teacher markedBy;

    // ── Getters for JSON ──────────────────────────────────────────────────────

    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonProperty("studentId")
    public Long fetchStudentId() {
        return student != null ? student.getId() : null;
    }

    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonProperty("subjectId")
    public Long fetchSubjectId() {
        return subject != null ? subject.getId() : null;
    }

    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonProperty("teacherId")
    public Long fetchTeacherId() {
        return markedBy != null ? markedBy.getId() : null;
    }
}
