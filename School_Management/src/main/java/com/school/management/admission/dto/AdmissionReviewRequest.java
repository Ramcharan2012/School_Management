package com.school.management.admission.dto;

import com.school.management.common.enums.AdmissionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdmissionReviewRequest {

    @NotNull(message = "Status is required")
    private AdmissionStatus status;

    private String adminRemarks;

    // Only used if APPROVED — specify which class to assign the student
    private Long classGradeId;
}
