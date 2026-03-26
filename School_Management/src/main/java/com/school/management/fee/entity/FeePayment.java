package com.school.management.fee.entity;

import com.school.management.common.entity.BaseEntity;
import com.school.management.common.enums.FeeStatus;
import com.school.management.common.enums.PaymentMethod;
import com.school.management.student.entity.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tracks an individual fee payment made by a student against a FeeStructure.
 * Supports partial payments via amountPaid vs amount due.
 */
@Audited
@Entity
@Table(name = "fee_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePayment extends BaseEntity {

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private FeeStatus status = FeeStatus.PENDING;

    @Column(name = "transaction_reference")
    private String transactionReference; // UPI/bank ref

    @Column(name = "receipt_number", unique = true)
    private String receiptNumber; // e.g., "RCPT-2024-00001", auto-generated

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "late_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;

    // ── Relationships ──────────────────────────────────────────────────────────

    @JsonIgnore
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @JsonIgnore
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;
}
