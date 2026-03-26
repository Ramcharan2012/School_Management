package com.school.management.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a student's rank in a class or school-wide leaderboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRankDto {
    private Integer rank;
    private Long studentId;
    private String fullName;
    private String rollNumber;
    private String className;
    private Double totalObtained;
    private Double totalMax;
    private Double percentage;
    private String overallGrade;
}
