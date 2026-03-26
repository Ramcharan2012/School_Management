package com.school.management.fee.repository;

import com.school.management.fee.entity.FeePayment;
import com.school.management.common.enums.FeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {
    Page<FeePayment> findByStudentId(Long studentId, Pageable pageable);

    Optional<FeePayment> findByReceiptNumber(String receiptNumber);

    @Query("SELECT SUM(fp.amountPaid) FROM FeePayment fp WHERE fp.student.id = :studentId AND fp.status = 'PAID'")
    BigDecimal totalPaidByStudentId(Long studentId);

    Page<FeePayment> findByStatus(FeeStatus status, Pageable pageable);

    @Query("SELECT SUM(fp.amountPaid) FROM FeePayment fp WHERE fp.paymentDate BETWEEN :from AND :to")
    BigDecimal getTotalPaidBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
