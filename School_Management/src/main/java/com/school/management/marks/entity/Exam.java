package com.school.management.marks.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.management.academic.entity.ClassGrade;
import com.school.management.academic.entity.Subject;
import com.school.management.common.entity.BaseEntity;
import com.school.management.common.enums.ExamType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a scheduled examination (e.g., Mid-Term Math exam for Grade 10-A).
 * One Exam can generate multiple Mark records (one per student).
 */
@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title; // e.g., "Mid-Term Mathematics 2024"

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "passing_marks", nullable = false)
    private Integer passingMarks;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false; // Results visible to students only when published

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_grade_id", nullable = false)
    private ClassGrade classGrade;

    @JsonIgnore
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Mark> marks = new ArrayList<>();
}
