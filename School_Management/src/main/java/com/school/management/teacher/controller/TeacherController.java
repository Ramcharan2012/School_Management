package com.school.management.teacher.controller;

import com.school.management.common.response.ApiResponse;
import com.school.management.common.response.PageResponse;
import com.school.management.teacher.entity.Teacher;
import com.school.management.teacher.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Teacher Management", description = "CRUD for teachers (Admin only)")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping("/admin/teachers")
    @Operation(summary = "Create a teacher account (Admin generates username + temp password)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTeacher(@RequestBody CreateTeacherRequest req) {
        Map<String, Object> result = teacherService.createTeacher(
                req.getFirstName(), req.getLastName(), req.getEmail(), req.getPhoneNumber(),
                req.getQualification(), req.getDesignation(), req.getSpecialization(),
                req.getExperienceYears(), req.getDepartmentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Teacher account created.", result));
    }

    @GetMapping("/admin/teachers")
    @Operation(summary = "List/search all teachers (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<Teacher>>> getAllTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        PageResponse<Teacher> result = (search != null && !search.isBlank())
                ? teacherService.searchTeachers(search, pageable)
                : teacherService.getAllTeachers(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/admin/teachers/{id}")
    public ResponseEntity<ApiResponse<Teacher>> getTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getTeacherById(id)));
    }

    @GetMapping("/teachers/{id}/profile")
    @Operation(summary = "Get my profile (Teacher self-view)")
    public ResponseEntity<ApiResponse<Teacher>> getTeacherProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getTeacherById(id)));
    }

    @PutMapping("/admin/teachers/{id}")
    public ResponseEntity<ApiResponse<Teacher>> updateTeacher(@PathVariable Long id,
            @RequestBody UpdateTeacherRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Teacher updated.",
                teacherService.updateTeacher(id, req.getQualification(), req.getDesignation(),
                        req.getSpecialization(), req.getExperienceYears(), req.getDepartmentId())));
    }

    @DeleteMapping("/admin/teachers/{id}/deactivate")
    @Operation(summary = "Deactivate a teacher account")
    public ResponseEntity<ApiResponse<Void>> deactivateTeacher(@PathVariable Long id) {
        teacherService.deactivateTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher deactivated."));
    }

    @Data
    static class CreateTeacherRequest {
        private String firstName, lastName, email, phoneNumber;
        private String qualification, designation, specialization;
        private Integer experienceYears;
        private Long departmentId;
    }

    @Data
    static class UpdateTeacherRequest {
        private String qualification, designation, specialization;
        private Integer experienceYears;
        private Long departmentId;
    }
}
