package com.school.management.admission.dto;

import com.school.management.common.enums.AdmissionStatus;
import com.school.management.common.enums.Gender;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AdmissionResponse {
    private Long id;
    private String applicationNumber;
    private String firstName;
    private String lastName;
    private String applicantEmail;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String parentName;
    private String parentPhone;
    private String parentEmail;
    private String applyingForGrade;
    private String academicYear;
    private String previousSchool;
    private BigDecimal admissionFee;
    private BigDecimal tuitionFeePerMonth;
    private Boolean feeConcessionRequested;
    private AdmissionStatus status;
    private String adminRemarks;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private Long generatedUserId;
    private LocalDateTime submittedAt;
}
