package com.school.management.leave.controller;

import com.school.management.common.enums.LeaveStatus;
import com.school.management.common.response.ApiResponse;
import com.school.management.common.response.PageResponse;
import com.school.management.leave.entity.LeaveRequest;
import com.school.management.leave.service.LeaveService;
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

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Leave application and approval")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/leave/apply")
    @Operation(summary = "Apply for leave (Teacher/Student)")
    public ResponseEntity<ApiResponse<LeaveRequest>> applyLeave(@RequestBody ApplyLeaveRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Leave application submitted.",
                leaveService.applyLeave(req.getApplicantId(), req.getSubject(),
                        req.getReason(), req.getFromDate(), req.getToDate())));
    }

    @GetMapping("/leave/my/{userId}")
    @Operation(summary = "View my leave applications")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequest>>> getMyLeaves(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.success(
                leaveService.getMyLeaves(userId, PageRequest.of(page, 10, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/admin/leave/pending")
    @Operation(summary = "View all pending leave requests (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequest>>> getPendingLeaves(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.success(
                leaveService.getPendingLeaves(PageRequest.of(page, 10, Sort.by("createdAt").ascending()))));
    }

    @GetMapping("/admin/leave")
    @Operation(summary = "View all leave requests (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequest>>> getAllLeaves(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.success(
                leaveService.getAllLeaves(PageRequest.of(page, 10, Sort.by("createdAt").descending()))));
    }

    @PatchMapping("/admin/leave/{id}/review")
    @Operation(summary = "Approve or reject a leave request (Admin only)")
    public ResponseEntity<ApiResponse<LeaveRequest>> reviewLeave(@PathVariable Long id,
            @RequestBody ReviewLeaveRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Leave " + req.getStatus() + ".",
                leaveService.reviewLeave(id, req.getStatus(), req.getAdminRemarks(), req.getReviewerUserId())));
    }

    @Data
    static class ApplyLeaveRequest {
        private Long applicantId;
        private String subject, reason;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate fromDate, toDate;
    }

    @Data
    static class ReviewLeaveRequest {
        private LeaveStatus status;
        private String adminRemarks;
        private Long reviewerUserId;
    }
}
