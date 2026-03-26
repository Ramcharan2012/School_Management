package com.school.management.analytics.dto;

import com.school.management.common.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * A single data point in a student's performance trend over time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentTrendDto {
    private Long examId;
    private String examTitle;
    private ExamType examType;
    private LocalDate examDate;
    private String subjectName;
    private Double marksObtained;
    private Integer totalMarks;
    private Double percentage;
    private String grade;
    private Boolean isAbsent;
}
