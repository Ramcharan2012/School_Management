package com.school.management.attendance.controller;

import com.school.management.attendance.entity.Attendance;
import com.school.management.attendance.service.AttendanceService;
import com.school.management.common.enums.AttendanceStatus;
import com.school.management.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Teacher marks attendance; students view their own")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/teacher/attendance/mark")
    @Operation(summary = "Mark student attendance (Teacher only)")
    public ResponseEntity<ApiResponse<Attendance>> markAttendance(@RequestBody MarkAttendanceRequest req) {
        Attendance result = attendanceService.markAttendance(
                req.getStudentId(), req.getSubjectId(), req.getTeacherId(),
                req.getDate(), req.getStatus(), req.getRemarks());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Attendance marked.", result));
    }

    @PatchMapping("/teacher/attendance/{id}")
    @Operation(summary = "Correct an attendance entry (Teacher only)")
    public ResponseEntity<ApiResponse<Attendance>> updateAttendance(@PathVariable Long id,
            @RequestBody UpdateAttendanceRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Attendance updated.",
                attendanceService.updateAttendance(id, req.getStatus(), req.getRemarks())));
    }

    @GetMapping("/teacher/attendance/subject/{subjectId}/date")
    @Operation(summary = "View attendance of all students for a subject on a date (Teacher)")
    public ResponseEntity<ApiResponse<List<Attendance>>> getBySubjectAndDate(
            @PathVariable Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceForSubjectOnDate(subjectId, date)));
    }

    @GetMapping("/student/attendance/{studentId}/subject/{subjectId}")
    @Operation(summary = "Get my attendance for a subject (Student view)")
    public ResponseEntity<ApiResponse<List<Attendance>>> getStudentAttendance(
            @PathVariable Long studentId, @PathVariable Long subjectId) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getStudentAttendanceBySubject(studentId, subjectId)));
    }

    @GetMapping("/student/attendance/{studentId}/subject/{subjectId}/summary")
    @Operation(summary = "Attendance summary with percentage (Student/Admin view)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendanceSummary(
            @PathVariable Long studentId, @PathVariable Long subjectId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceSummary(studentId, subjectId)));
    }

    @Data
    static class MarkAttendanceRequest {
        private Long studentId, subjectId, teacherId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate date;
        private AttendanceStatus status;
        private String remarks;
    }

    @Data
    static class UpdateAttendanceRequest {
        private AttendanceStatus status;
        private String remarks;
    }
}
