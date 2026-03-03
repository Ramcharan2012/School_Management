package com.school.management.student.controller;

import com.school.management.common.response.ApiResponse;
import com.school.management.common.response.PageResponse;
import com.school.management.student.entity.Student;
import com.school.management.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Student Management")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/admin/students")
    @Operation(summary = "List/search all students (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<Student>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long classGradeId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        PageResponse<Student> result;
        if (search != null && !search.isBlank())
            result = studentService.searchStudents(search, pageable);
        else if (classGradeId != null)
            result = studentService.getStudentsByClass(classGradeId, pageable);
        else
            result = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/admin/students/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentById(id)));
    }

    @GetMapping("/students/{id}/profile")
    @Operation(summary = "Get my profile (Student self-view)")
    public ResponseEntity<ApiResponse<Student>> getStudentProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentById(id)));
    }

    @PatchMapping("/admin/students/{id}/transfer")
    @Operation(summary = "Transfer student to a different class (Admin only)")
    public ResponseEntity<ApiResponse<Student>> transferStudent(@PathVariable Long id,
            @RequestParam Long newClassGradeId) {
        return ResponseEntity.ok(ApiResponse.success("Student transferred.",
                studentService.transferStudentToClass(id, newClassGradeId)));
    }

    @PatchMapping("/students/{id}/profile")
    @Operation(summary = "Update own contact info (Student/Admin)")
    public ResponseEntity<ApiResponse<Student>> updateProfile(@PathVariable Long id,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated.",
                studentService.updateStudentProfile(id, req.getAddress(), req.getParentPhone(),
                        req.getEmergencyContact())));
    }

    @DeleteMapping("/admin/students/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        studentService.deactivateStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deactivated."));
    }

    @Data
    static class UpdateProfileRequest {
        private String address, parentPhone, emergencyContact;
    }
}
