package com.school.management.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Attendance health record — flags students with low attendance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceHealthDto {
    private Long studentId;
    private String fullName;
    private String rollNumber;
    private Long totalClasses;
    private Long daysPresent;
    private Long daysAbsent;
    private Long daysLate;
    private Double attendancePct;
    private Boolean flagged; // true if < 75%
}
