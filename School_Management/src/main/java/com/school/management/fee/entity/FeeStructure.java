package com.school.management.fee.entity;

import com.school.management.academic.entity.AcademicYear;
import com.school.management.academic.entity.ClassGrade;
import com.school.management.common.entity.BaseEntity;
import com.school.management.common.enums.FeeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Defines the fee structure for a specific class grade in an academic year.
 * Example: Tuition fee of ₹5000/month for Grade 10 in 2024-2025.
 */
@Entity
@Table(name = "fee_structures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructure extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private FeeType feeType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "description")
    private String description;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_grade_id")
    private ClassGrade classGrade; // null means applicable to ALL classes
}
