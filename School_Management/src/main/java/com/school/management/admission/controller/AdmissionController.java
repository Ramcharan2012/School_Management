package com.school.management.admission.controller;

import com.school.management.admission.dto.AdmissionFormRequest;
import com.school.management.admission.dto.AdmissionReviewRequest;
import com.school.management.admission.dto.AdmissionResponse;
import com.school.management.admission.service.AdmissionService;
import com.school.management.common.enums.AdmissionStatus;
import com.school.management.common.response.ApiResponse;
import com.school.management.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admission", description = "Admission form submission and admin review APIs")
public class AdmissionController {

    private final AdmissionService admissionService;

    // ── PUBLIC ENDPOINTS ──────────────────────────────────────────────────────

    @PostMapping("/public/admissions/apply")
    @Operation(summary = "Submit admission application (Public — no login required)")
    public ResponseEntity<ApiResponse<AdmissionResponse>> submitApplication(
            @Valid @RequestBody AdmissionFormRequest request) {
        AdmissionResponse response = admissionService.submitAdmissionForm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully. Your application number is: "
                        + response.getApplicationNumber(), response));
    }

    @GetMapping("/public/admissions/status/{applicationNumber}")
    @Operation(summary = "Track admission status by application number (Public)")
    public ResponseEntity<ApiResponse<AdmissionResponse>> trackStatus(@PathVariable String applicationNumber) {
        return ResponseEntity.ok(ApiResponse.success(admissionService.getByApplicationNumber(applicationNumber)));
    }

    // ── ADMIN ENDPOINTS ───────────────────────────────────────────────────────

    @GetMapping("/admin/admissions")
    @Operation(summary = "List all admissions with pagination (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<AdmissionResponse>>> getAllAdmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(required = false) String search) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<AdmissionResponse> result;

        if (search != null && !search.isBlank()) {
            result = admissionService.searchAdmissions(search, pageable);
        } else if (status != null) {
            result = admissionService.getAdmissionsByStatus(status, pageable);
        } else {
            result = admissionService.getAllAdmissions(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/admin/admissions/{id}/review")
    @Operation(summary = "Approve or reject an admission (Admin only)")
    public ResponseEntity<ApiResponse<AdmissionResponse>> reviewAdmission(
            @PathVariable Long id,
            @Valid @RequestBody AdmissionReviewRequest reviewRequest,
            Principal principal) {
        String reviewer = principal != null ? principal.getName() : "ADMIN";
        AdmissionResponse response = admissionService.reviewAdmission(id, reviewRequest, reviewer);
        return ResponseEntity.ok(ApiResponse.success("Admission " + reviewRequest.getStatus(), response));
    }

    @GetMapping("/admin/admissions/stats")
    @Operation(summary = "Admission statistics — count by status (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(admissionService.getAdmissionStats()));
    }
}
