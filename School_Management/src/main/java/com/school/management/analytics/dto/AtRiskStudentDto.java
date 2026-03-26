package com.school.management.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * At-risk student — combines low attendance AND low academic performance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtRiskStudentDto {
    private Long studentId;
    private String fullName;
    private String rollNumber;
    private String className;
    private Double attendancePct;
    private Double academicPercentage;
    private Boolean lowAttendance;   // < 75%
    private Boolean lowPerformance;  // < 40%
    private String riskLevel;        // "HIGH", "MEDIUM"
}
