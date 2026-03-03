package com.school.management.fee.controller;

import com.school.management.common.enums.FeeType;
import com.school.management.common.enums.PaymentMethod;
import com.school.management.common.response.ApiResponse;
import com.school.management.common.response.PageResponse;
import com.school.management.fee.entity.FeePayment;
import com.school.management.fee.entity.FeeStructure;
import com.school.management.fee.service.FeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Fee Management", description = "Fee structures and payment tracking")
public class FeeController {

    private final FeeService feeService;

    @PostMapping("/admin/fees/structures")
    @Operation(summary = "Create a fee structure (Admin only)")
    public ResponseEntity<ApiResponse<FeeStructure>> createFeeStructure(@RequestBody FeeStructureRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Fee structure created.",
                feeService.createFeeStructure(req.getFeeType(), req.getAmount(), req.getDueDate(),
                        req.getDescription(), req.getIsMandatory(), req.getAcademicYearId(), req.getClassGradeId())));
    }

    @GetMapping("/admin/fees/structures")
    public ResponseEntity<ApiResponse<List<FeeStructure>>> getFeeStructures(@RequestParam Long academicYearId) {
        return ResponseEntity.ok(ApiResponse.success(feeService.getAllFeeStructures(academicYearId)));
    }

    @PostMapping("/admin/fees/payments")
    @Operation(summary = "Record a fee payment (Admin/Accountant)")
    public ResponseEntity<ApiResponse<FeePayment>> recordPayment(@RequestBody FeePaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Payment recorded.",
                feeService.recordPayment(req.getStudentId(), req.getFeeStructureId(),
                        req.getAmountPaid(), req.getPaymentMethod(),
                        req.getTransactionReference(), req.getRemarks())));
    }

    @GetMapping("/student/fees/{studentId}/payments")
    @Operation(summary = "View fee payment history (Student/Admin)")
    public ResponseEntity<ApiResponse<PageResponse<FeePayment>>> getPayments(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.success(
                feeService.getStudentPayments(studentId,
                        PageRequest.of(page, 10, Sort.by("paymentDate").descending()))));
    }

    @GetMapping("/student/fees/{studentId}/statement")
    @Operation(summary = "Full fee statement: total due, paid, balance (Student/Admin)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeeStatement(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(feeService.getStudentFeeStatement(studentId)));
    }

    @GetMapping("/student/fees/{studentId}/applicable")
    @Operation(summary = "Get all applicable fee structures for a student")
    public ResponseEntity<ApiResponse<List<FeeStructure>>> getApplicableFees(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(feeService.getFeeStructureForStudent(studentId)));
    }

    @Data
    static class FeeStructureRequest {
        private FeeType feeType;
        private BigDecimal amount;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dueDate;
        private String description;
        private Boolean isMandatory = true;
        private Long academicYearId, classGradeId;
    }

    @Data
    static class FeePaymentRequest {
        private Long studentId, feeStructureId;
        private BigDecimal amountPaid;
        private PaymentMethod paymentMethod;
        private String transactionReference, remarks;
    }
}
