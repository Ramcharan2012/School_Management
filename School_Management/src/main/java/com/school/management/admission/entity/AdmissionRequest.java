package com.school.management.admission.entity;

import com.school.management.common.entity.BaseEntity;
import com.school.management.common.enums.AdmissionStatus;
import com.school.management.common.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Stores a student's admission application submitted via the public enrollment
 * form.
 *
 * Flow: Student submits → PENDING → Admin reviews → APPROVED or REJECTED
 * On APPROVED: System auto-generates User + Student profile + sends credentials
 * by email.
 */
@Entity
@Table(name = "admission_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionRequest extends BaseEntity {

    // ── Applicant Personal Information ─────────────────────────────────────────

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email
    @Column(name = "applicant_email", nullable = false)
    private String applicantEmail;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "blood_group")
    private String bloodGroup;

    // ── Parent / Guardian Information ──────────────────────────────────────────

    @Column(name = "parent_name", nullable = false)
    private String parentName;

    @Column(name = "parent_phone", nullable = false)
    private String parentPhone;

    @Email
    @Column(name = "parent_email")
    private String parentEmail;

    @Column(name = "parent_occupation")
    private String parentOccupation;

    // ── Academic Information ───────────────────────────────────────────────────

    @Column(name = "applying_for_grade", nullable = false)
    private String applyingForGrade; // e.g., "Grade 6"

    @Column(name = "academic_year", nullable = false)
    private String academicYear; // e.g., "2024-2025"

    @Column(name = "previous_school")
    private String previousSchool;

    @Column(name = "previous_class_completed")
    private String previousClassCompleted;

    @Column(name = "previous_percentage")
    private Double previousPercentage;

    // ── Fee Details ────────────────────────────────────────────────────────────

    @Column(name = "admission_fee", precision = 10, scale = 2)
    private BigDecimal admissionFee;

    @Column(name = "tuition_fee_per_month", precision = 10, scale = 2)
    private BigDecimal tuitionFeePerMonth;

    @Column(name = "other_charges", precision = 10, scale = 2)
    private BigDecimal otherCharges;

    @Column(name = "fee_concession_requested")
    @Builder.Default
    private Boolean feeConcessionRequested = false;

    @Column(name = "fee_concession_reason", columnDefinition = "TEXT")
    private String feeConcessionReason;

    // ── Application Status & Admin Review ─────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AdmissionStatus status = AdmissionStatus.PENDING;

    @Column(name = "admin_remarks", columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private java.time.LocalDateTime reviewedAt;

    // ── Link to created user (set after approval) ──────────────────────────────

    @Column(name = "generated_user_id")
    private Long generatedUserId;

    @Column(name = "application_number", unique = true)
    private String applicationNumber; // e.g., "APP-2024-00001"
}
