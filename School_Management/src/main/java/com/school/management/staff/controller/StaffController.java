package com.school.management.staff.controller;

import com.school.management.common.enums.StaffCategory;
import com.school.management.common.response.ApiResponse;
import com.school.management.common.response.PageResponse;
import com.school.management.staff.entity.Staff;
import com.school.management.staff.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for Non-Teaching Staff management.
 * All endpoints require ADMIN role (enforced via SecurityConfig).
 */
@RestController
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
@Tag(name = "Non-Teaching Staff", description = "Manage non-teaching staff members (librarians, accountants, etc.)")
@SecurityRequirement(name = "bearerAuth")
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    @Operation(summary = "Create a non-teaching staff member and generate login credentials")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStaff(
            @RequestBody CreateStaffRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Staff member created successfully.",
                staffService.createStaff(
                        req.getFirstName(), req.getLastName(), req.getEmail(),
                        req.getPhoneNumber(), req.getStaffCategory(), req.getDesignation(),
                        req.getQualification(), req.getDepartmentId())));
    }

    @GetMapping
    @Operation(summary = "List all active staff (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<Staff>>> getAllStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                staffService.getAllStaff(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filter staff by category (e.g. LIBRARIAN, SECURITY)")
    public ResponseEntity<ApiResponse<PageResponse<Staff>>> getByCategory(
            @PathVariable StaffCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                staffService.getStaffByCategory(category,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single staff member by ID")
    public ResponseEntity<ApiResponse<Staff>> getStaffById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(staffService.getStaffById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update staff designation, qualification, category or department")
    public ResponseEntity<ApiResponse<Staff>> updateStaff(
            @PathVariable Long id,
            @RequestBody UpdateStaffRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Staff updated.",
                staffService.updateStaff(id, req.getDesignation(), req.getQualification(),
                        req.getStaffCategory(), req.getDepartmentId())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a staff member (soft delete — account remains, login disabled)")
    public ResponseEntity<ApiResponse<Void>> deactivateStaff(@PathVariable Long id) {
        staffService.deactivateStaff(id);
        return ResponseEntity.ok(ApiResponse.success("Staff member deactivated."));
    }

    // ── Inner Request DTOs ────────────────────────────────────────────────────

    @Data
    static class CreateStaffRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private StaffCategory staffCategory;
        private String designation;
        private String qualification;
        private Long departmentId;   // optional
    }

    @Data
    static class UpdateStaffRequest {
        private String designation;
        private String qualification;
        private StaffCategory staffCategory;
        private Long departmentId;
    }
}
