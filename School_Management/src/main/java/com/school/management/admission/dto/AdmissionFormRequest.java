package com.school.management.admission.dto;

import com.school.management.common.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AdmissionFormRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String applicantEmail;

    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String bloodGroup;

    @NotBlank(message = "Parent name is required")
    private String parentName;

    @NotBlank(message = "Parent phone is required")
    private String parentPhone;

    @Email(message = "Valid parent email required")
    private String parentEmail;

    private String parentOccupation;

    @NotBlank(message = "Grade applying for is required")
    private String applyingForGrade;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    private String previousSchool;
    private String previousClassCompleted;
    private Double previousPercentage;

    private BigDecimal admissionFee;
    private BigDecimal tuitionFeePerMonth;
    private BigDecimal otherCharges;
    private Boolean feeConcessionRequested = false;
    private String feeConcessionReason;
}
