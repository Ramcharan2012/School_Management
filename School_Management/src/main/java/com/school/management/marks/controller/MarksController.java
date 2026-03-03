package com.school.management.marks.controller;

import com.school.management.common.enums.ExamType;
import com.school.management.common.response.ApiResponse;
import com.school.management.common.response.PageResponse;
import com.school.management.marks.entity.Exam;
import com.school.management.marks.entity.Mark;
import com.school.management.marks.service.MarksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Marks & Exams", description = "Exam management and mark entry")
public class MarksController {

    private final MarksService marksService;

    // ── Admin/Teacher: Exam Management ───────────────────────────────────────

    @PostMapping("/admin/exams")
    @Operation(summary = "Create an exam (Admin/Teacher)")
    public ResponseEntity<ApiResponse<Exam>> createExam(@RequestBody CreateExamRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Exam created.",
                marksService.createExam(req.getTitle(), req.getExamType(), req.getExamDate(),
                        req.getTotalMarks(), req.getPassingMarks(), req.getDurationMinutes(),
                        req.getDescription(), req.getSubjectId(), req.getClassGradeId())));
    }

    @GetMapping("/admin/exams/class/{classGradeId}")
    public ResponseEntity<ApiResponse<PageResponse<Exam>>> getExams(
            @PathVariable Long classGradeId,
            @RequestParam(defaultValue = "false") boolean publishedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                marksService.getExamsByClass(classGradeId, publishedOnly,
                        PageRequest.of(page, size, Sort.by("examDate").descending()))));
    }

    @PatchMapping("/admin/exams/{examId}/publish")
    @Operation(summary = "Publish exam results so students can see them")
    public ResponseEntity<ApiResponse<Exam>> publishResults(@PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.success("Results published.", marksService.publishExamResults(examId)));
    }

    // ── Teacher: Enter/Update Marks ───────────────────────────────────────────

    @PostMapping("/teacher/marks")
    @Operation(summary = "Enter marks for a student in an exam (Teacher only)")
    public ResponseEntity<ApiResponse<Mark>> enterMark(@RequestBody EnterMarkRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Marks entered.",
                marksService.enterMark(req.getStudentId(), req.getExamId(), req.getTeacherId(),
                        req.getMarksObtained(), req.getIsAbsent(), req.getRemarks())));
    }

    @PatchMapping("/teacher/marks/{markId}")
    @Operation(summary = "Correct a mark entry (Teacher only)")
    public ResponseEntity<ApiResponse<Mark>> updateMark(@PathVariable Long markId, @RequestBody UpdateMarkRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Mark updated.",
                marksService.updateMark(markId, req.getMarksObtained(), req.getRemarks())));
    }

    // ── Student: View Marks ───────────────────────────────────────────────────

    @GetMapping("/student/marks/{studentId}")
    @Operation(summary = "View my marks (Student view — only published exams)")
    public ResponseEntity<ApiResponse<PageResponse<Mark>>> getStudentMarks(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.success(
                marksService.getStudentMarks(studentId, PageRequest.of(page, 10, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/student/marks/{studentId}/report-card")
    @Operation(summary = "Get full report card with overall grade and percentage")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReportCard(
            @PathVariable Long studentId, @RequestParam Long classGradeId) {
        return ResponseEntity.ok(ApiResponse.success(marksService.getStudentReportCard(studentId, classGradeId)));
    }

    @GetMapping("/teacher/marks/exam/{examId}/results")
    @Operation(summary = "View all results for an exam (Teacher/Admin)")
    public ResponseEntity<ApiResponse<List<Mark>>> getExamResults(@PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.success(marksService.getExamResults(examId)));
    }

    @Data
    static class CreateExamRequest {
        private String title, description;
        private ExamType examType;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate examDate;
        private Integer totalMarks, passingMarks, durationMinutes;
        private Long subjectId, classGradeId;
    }

    @Data
    static class EnterMarkRequest {
        private Long studentId, examId, teacherId;
        private Double marksObtained;
        private Boolean isAbsent = false;
        private String remarks;
    }

    @Data
    static class UpdateMarkRequest {
        private Double marksObtained;
        private String remarks;
    }
}
