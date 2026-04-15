package com.school.management.leave.entity;

import com.school.management.common.entity.BaseEntity;
import com.school.management.common.enums.LeaveStatus;
import com.school.management.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import lombok.*;

import java.time.LocalDate;

/**
 * Leave application submitted by a Teacher or Student.
 * Reviewed and approved/rejected by Admin.
 */
@Audited
@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "total_days")
    private Integer totalDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "admin_remarks", columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(name = "reviewed_at")
    private java.time.LocalDateTime reviewedAt;

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @JsonIgnore
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", nullable = true)
    private User reviewedBy;

    // ── Getters for JSON ──────────────────────────────────────────────────────

    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonProperty("applicantId")
    public Long getApplicantId() {
        return applicant != null ? applicant.getId() : null;
    }

    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonProperty("applicantName")
    public String getApplicantName() {
        if (applicant != null) {
            return applicant.getFirstName() + " " + applicant.getLastName();
        }
        return null;
    }

    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonProperty("applicantRole")
    public String getApplicantRole() {
        return applicant != null ? applicant.getRole().name() : null;
    }

    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonProperty("reviewerId")
    public Long getReviewerId() {
        return reviewedBy != null ? reviewedBy.getId() : null;
    }
}
