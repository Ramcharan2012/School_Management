package com.school.management.academic.controller;

import com.school.management.academic.entity.*;
import com.school.management.academic.service.AcademicService;
import com.school.management.common.response.ApiResponse;
import com.school.management.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/academic")
@RequiredArgsConstructor
@Tag(name = "Academic Setup", description = "Departments, Academic Years, Classes, Subjects (Admin only)")
public class AcademicController {

    private final AcademicService academicService;

    // ── Departments ───────────────────────────────────────────────────────────
    @PostMapping("/departments")
    @Operation(summary = "Create a department")
    public ResponseEntity<ApiResponse<Department>> createDepartment(@RequestBody DepartmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse
                        .success(academicService.createDepartment(req.getName(), req.getCode(), req.getDescription())));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.success(academicService.getAllDepartments()));
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Department>> getDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(academicService.getDepartmentById(id)));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(@PathVariable Long id,
            @RequestBody DepartmentRequest req) {
        return ResponseEntity
                .ok(ApiResponse.success(academicService.updateDepartment(id, req.getName(), req.getDescription())));
    }

    // ── Academic Years ────────────────────────────────────────────────────────
    @PostMapping("/years")
    @Operation(summary = "Create an academic year")
    public ResponseEntity<ApiResponse<AcademicYear>> createYear(@RequestBody AcademicYearRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        academicService.createAcademicYear(req.getYearLabel(), req.getStartDate(), req.getEndDate())));
    }

    @GetMapping("/years")
    public ResponseEntity<ApiResponse<List<AcademicYear>>> getAllYears() {
        return ResponseEntity.ok(ApiResponse.success(academicService.getAllAcademicYears()));
    }

    @GetMapping("/years/active")
    public ResponseEntity<ApiResponse<AcademicYear>> getActiveYear() {
        return ResponseEntity.ok(ApiResponse.success(academicService.getActiveAcademicYear()));
    }

    @PatchMapping("/years/{id}/activate")
    @Operation(summary = "Set a year as active (deactivates previous active year)")
    public ResponseEntity<ApiResponse<AcademicYear>> activateYear(@PathVariable Long id) {
        return ResponseEntity
                .ok(ApiResponse.success("Academic year activated.", academicService.setActiveAcademicYear(id)));
    }

    // ── Classes ───────────────────────────────────────────────────────────────
    @PostMapping("/classes")
    @Operation(summary = "Create a class grade + section")
    public ResponseEntity<ApiResponse<ClassGrade>> createClass(@RequestBody ClassGradeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.createClassGrade(req.getGradeName(), req.getSection(),
                        req.getCapacity(), req.getRoomNumber(), req.getAcademicYearId())));
    }

    @GetMapping("/classes")
    public ResponseEntity<ApiResponse<PageResponse<ClassGrade>>> getClasses(
            @RequestParam Long academicYearId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getClassesPaged(academicYearId, PageRequest.of(page, size, Sort.by("gradeName")))));
    }

    @PatchMapping("/classes/{classGradeId}/assign-teacher/{teacherId}")
    @Operation(summary = "Assign a class teacher to a class grade")
    public ResponseEntity<ApiResponse<ClassGrade>> assignClassTeacher(@PathVariable Long classGradeId,
            @PathVariable Long teacherId) {
        return ResponseEntity.ok(ApiResponse.success("Class teacher assigned.",
                academicService.assignClassTeacher(classGradeId, teacherId)));
    }

    // ── Subjects ──────────────────────────────────────────────────────────────
    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<Subject>> createSubject(@RequestBody SubjectRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.createSubject(req.getName(), req.getCode(), req.getDescription(), req.getCreditHours(),
                        req.getDepartmentId())));
    }

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<PageResponse<Subject>>> getSubjects(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity
                .ok(ApiResponse.success(academicService.getAllSubjects(PageRequest.of(page, size, Sort.by("name")))));
    }

    // ── Subject Assignments ───────────────────────────────────────────────────
    @PostMapping("/assignments")
    @Operation(summary = "Assign a teacher to teach a subject in a class")
    public ResponseEntity<ApiResponse<SubjectAssignment>> assignTeacher(@RequestBody SubjectAssignmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.assignTeacherToSubject(req.getTeacherId(), req.getSubjectId(), req.getClassGradeId())));
    }

    @GetMapping("/assignments/class/{classGradeId}")
    public ResponseEntity<ApiResponse<List<SubjectAssignment>>> getAssignmentsByClass(@PathVariable Long classGradeId) {
        return ResponseEntity.ok(ApiResponse.success(academicService.getAssignmentsByClass(classGradeId)));
    }

    @GetMapping("/assignments/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<SubjectAssignment>>> getAssignmentsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(ApiResponse.success(academicService.getAssignmentsByTeacher(teacherId)));
    }

    // ── Inner request DTOs ────────────────────────────────────────────────────
    @Data
    static class DepartmentRequest {
        private String name, code, description;
    }

    @Data
    static class AcademicYearRequest {
        private String yearLabel;
        private LocalDate startDate, endDate;
    }

    @Data
    static class ClassGradeRequest {
        private String gradeName, section, roomNumber;
        private Integer capacity;
        private Long academicYearId;
    }

    @Data
    static class SubjectRequest {
        private String name, code, description;
        private Integer creditHours;
        private Long departmentId;
    }

    @Data
    static class SubjectAssignmentRequest {
        private Long teacherId, subjectId, classGradeId;
    }
}
