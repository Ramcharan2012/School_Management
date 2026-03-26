package com.school.management.analytics.controller;

import com.school.management.analytics.dto.*;
import com.school.management.analytics.service.AnalyticsService;
import com.school.management.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Analytics endpoints — ADMIN only.
 * Provides school-wide and class-level insights into student performance and attendance.
 */
@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Student Analytics", description = "School-wide student performance insights (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-performers")
    @Operation(summary = "School-wide top performers ranked by overall percentage")
    public ResponseEntity<ApiResponse<List<StudentRankDto>>> getTopPerformers(
            @RequestParam(defaultValue = "10") int n) {
        return ResponseEntity.ok(ApiResponse.success(
                "Top " + n + " performers fetched.",
                analyticsService.getSchoolTopPerformers(n)));
    }

    @GetMapping("/class/{classGradeId}/ranking")
    @Operation(summary = "Rank all students in a class for a specific exam")
    public ResponseEntity<ApiResponse<List<StudentRankDto>>> getClassRanking(
            @PathVariable Long classGradeId,
            @RequestParam Long examId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Class ranking fetched.",
                analyticsService.getClassRanking(classGradeId, examId)));
    }

    @GetMapping("/class/{classGradeId}/attendance-health")
    @Operation(summary = "Attendance health report for a class — flags students below 75%")
    public ResponseEntity<ApiResponse<List<AttendanceHealthDto>>> getAttendanceHealth(
            @PathVariable Long classGradeId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attendance health report fetched.",
                analyticsService.getAttendanceHealth(classGradeId)));
    }

    @GetMapping("/exam/{examId}/subject-analytics")
    @Operation(summary = "Subject-level statistics for a specific exam (avg, highest, lowest, pass rate)")
    public ResponseEntity<ApiResponse<SubjectAnalyticsDto>> getSubjectAnalytics(
            @PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subject analytics fetched.",
                analyticsService.getSubjectAnalytics(examId)));
    }

    @GetMapping("/student/{studentId}/trend")
    @Operation(summary = "Student's exam-by-exam performance trend (optionally filtered by subject)")
    public ResponseEntity<ApiResponse<List<StudentTrendDto>>> getStudentTrend(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long subjectId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Student trend data fetched.",
                analyticsService.getStudentTrend(studentId, subjectId)));
    }

    @GetMapping("/class/{classGradeId}/at-risk")
    @Operation(summary = "At-risk students (low attendance + low performance combined)")
    public ResponseEntity<ApiResponse<List<AtRiskStudentDto>>> getAtRiskStudents(
            @PathVariable Long classGradeId) {
        return ResponseEntity.ok(ApiResponse.success(
                "At-risk students fetched.",
                analyticsService.getAtRiskStudents(classGradeId)));
    }
}
