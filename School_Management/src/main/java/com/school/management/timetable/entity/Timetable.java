package com.school.management.timetable.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.management.academic.entity.ClassGrade;
import com.school.management.academic.entity.Subject;
import com.school.management.common.entity.BaseEntity;
import com.school.management.teacher.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Weekly timetable slot for a specific class-subject-teacher combination.
 * Example: Grade 10-A, Monday, 9:00 AM - 10:00 AM, Mathematics, Mr. Sharma.
 */
@Entity
@Table(name = "timetables", uniqueConstraints = @UniqueConstraint(columnNames = { "class_grade_id", "day_of_week",
        "start_time" }, name = "uk_timetable_class_day_time"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timetable extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_grade_id", nullable = false)
    private ClassGrade classGrade;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    // ── JSON helpers ──────────────────────────────────────────────────────────

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("classGradeId")
    public Long fetchClassGradeId() { return classGrade != null ? classGrade.getId() : null; }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("subject")
    public java.util.Map<String, Object> fetchSubject() {
        if (subject == null) return null;
        return java.util.Map.of("id", subject.getId(), "name", subject.getName(), "code", subject.getCode() != null ? subject.getCode() : "");
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("teacher")
    public java.util.Map<String, Object> fetchTeacher() {
        if (teacher == null) return null;
        return java.util.Map.of(
            "id", teacher.getId(),
            "user", java.util.Map.of(
                "firstName", teacher.getUser() != null ? teacher.getUser().getFirstName() : "",
                "lastName", teacher.getUser() != null ? teacher.getUser().getLastName() : ""
            )
        );
    }
}
