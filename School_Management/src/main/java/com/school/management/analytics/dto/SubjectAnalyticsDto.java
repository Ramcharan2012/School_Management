package com.school.management.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subject-level analytics for an exam: average, highest, lowest, pass rate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectAnalyticsDto {
    private Long examId;
    private String examTitle;
    private String subjectName;
    private Integer totalMarks;
    private Double classAverage;
    private Double highest;
    private Double lowest;
    private Long totalStudents;
    private Long passed;
    private Double passRate;
}
