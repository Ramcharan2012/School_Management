package com.school.management.fee.service;

import com.school.management.academic.entity.AcademicYear;
import com.school.management.academic.entity.ClassGrade;
import com.school.management.academic.repository.AcademicYearRepository;
import com.school.management.academic.repository.ClassGradeRepository;
import com.school.management.common.enums.FeeStatus;
import com.school.management.common.enums.FeeType;
import com.school.management.common.enums.PaymentMethod;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.response.PageResponse;
import com.school.management.common.util.IdGeneratorUtil;
import com.school.management.fee.entity.FeePayment;
import com.school.management.fee.entity.FeeStructure;
import com.school.management.fee.repository.FeePaymentRepository;
import com.school.management.fee.repository.FeeStructureRepository;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeStructureRepository feeStructureRepo;
    private final FeePaymentRepository feePaymentRepo;
    private final StudentRepository studentRepo;
    private final AcademicYearRepository academicYearRepo;
    private final ClassGradeRepository classGradeRepo;

    @Transactional
    public FeeStructure createFeeStructure(FeeType feeType, BigDecimal amount, LocalDate dueDate,
            String description, Boolean isMandatory,
            Long academicYearId, Long classGradeId) {
        AcademicYear ay = academicYearRepo.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", academicYearId));
        ClassGrade cg = classGradeId != null ? classGradeRepo.findById(classGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", classGradeId)) : null;
        return feeStructureRepo.save(FeeStructure.builder()
                .feeType(feeType).amount(amount).dueDate(dueDate).description(description)
                .isMandatory(isMandatory).academicYear(ay).classGrade(cg).isActive(true)
                .build());
    }

    public List<FeeStructure> getFeeStructureForStudent(Long studentId) {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Long ayId = student.getClassGrade().getAcademicYear().getId();
        Long cgId = student.getClassGrade().getId();
        List<FeeStructure> classSpecific = feeStructureRepo.findByClassGradeIdAndAcademicYearId(cgId, ayId);
        List<FeeStructure> global = feeStructureRepo.findByClassGradeIdIsNullAndAcademicYearId(ayId);
        classSpecific.addAll(global);
        return classSpecific;
    }

    @Transactional
    public FeePayment recordPayment(Long studentId, Long feeStructureId, BigDecimal amountPaid,
            PaymentMethod method, String transactionRef, String remarks) {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        FeeStructure feeStructure = feeStructureRepo.findById(feeStructureId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", feeStructureId));

        FeeStatus status = amountPaid.compareTo(feeStructure.getAmount()) >= 0
                ? FeeStatus.PAID
                : FeeStatus.PARTIALLY_PAID;

        return feePaymentRepo.save(FeePayment.builder()
                .student(student).feeStructure(feeStructure)
                .amountPaid(amountPaid).paymentDate(LocalDate.now())
                .paymentMethod(method).transactionReference(transactionRef)
                .receiptNumber(IdGeneratorUtil.generateReceiptNumber())
                .remarks(remarks).status(status)
                .build());
    }

    public PageResponse<FeePayment> getStudentPayments(Long studentId, Pageable pageable) {
        return PageResponse.of(feePaymentRepo.findByStudentId(studentId, pageable));
    }

    public Map<String, Object> getStudentFeeStatement(Long studentId) {
        List<FeeStructure> structures = getFeeStructureForStudent(studentId);
        BigDecimal totalDue = structures.stream()
                .map(FeeStructure::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = feePaymentRepo.totalPaidByStudentId(studentId);
        if (totalPaid == null)
            totalPaid = BigDecimal.ZERO;
        BigDecimal balance = totalDue.subtract(totalPaid);

        return Map.of("studentId", studentId, "totalDue", totalDue,
                "totalPaid", totalPaid, "balance", balance,
                "feeStatus", balance.compareTo(BigDecimal.ZERO) <= 0 ? "CLEAR" : "DUES_PENDING");
    }

    public List<FeeStructure> getAllFeeStructures(Long academicYearId) {
        return feeStructureRepo.findByAcademicYearId(academicYearId);
    }
}
