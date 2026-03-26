package com.school.management.analytics.service;

import com.school.management.analytics.dto.*;
import com.school.management.attendance.repository.AttendanceRepository;
import com.school.management.common.enums.AttendanceStatus;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.marks.entity.Exam;
import com.school.management.marks.entity.Mark;
import com.school.management.marks.repository.ExamRepository;
import com.school.management.marks.repository.MarkRepository;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Analytics service — computes rankings, attendance health, subject stats,
 * student performance trends, and at-risk detection using existing entities.
 * READ-ONLY — no write operations here, no new DB tables needed.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final StudentRepository studentRepo;
    private final MarkRepository markRepo;
    private final ExamRepository examRepo;
    private final AttendanceRepository attendanceRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. School-wide Top Performers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns top N students school-wide ranked by overall percentage
     * (total marks obtained / total max marks across all published exams).
     */
    public List<StudentRankDto> getSchoolTopPerformers(int topN) {
        List<Student> allStudents = studentRepo.findAll();
        List<StudentRankDto> ranked = new ArrayList<>();

        for (Student student : allStudents) {
            List<Mark> marks = markRepo.findByStudentId(student.getId());
            double obtained = marks.stream()
                    .filter(m -> !m.getIsAbsent() && m.getExam().getIsPublished())
                    .mapToDouble(Mark::getMarksObtained).sum();
            double maxMarks = marks.stream()
                    .filter(m -> !m.getIsAbsent() && m.getExam().getIsPublished())
                    .mapToDouble(m -> m.getExam().getTotalMarks()).sum();

            if (maxMarks == 0) continue;
            double pct = Math.round((obtained / maxMarks * 100) * 10.0) / 10.0;
            String className = student.getClassGrade() != null
                    ? student.getClassGrade().getGradeName() + "-" + student.getClassGrade().getSection()
                    : "N/A";

            ranked.add(StudentRankDto.builder()
                    .studentId(student.getId())
                    .fullName(student.getUser() != null ? student.getUser().getFullName() : student.getRollNumber())
                    .rollNumber(student.getRollNumber())
                    .className(className)
                    .totalObtained(obtained)
                    .totalMax(maxMarks)
                    .percentage(pct)
                    .overallGrade(gradeFromPct(pct))
                    .build());
        }

        // Sort desc by percentage, assign rank, limit to N
        ranked.sort(Comparator.comparingDouble(StudentRankDto::getPercentage).reversed());
        AtomicInteger rankCounter = new AtomicInteger(1);
        return ranked.stream()
                .limit(topN)
                .peek(r -> r.setRank(rankCounter.getAndIncrement()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Class Ranking for a specific Exam
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ranks all students in a class by marks in a specific exam.
     */
    public List<StudentRankDto> getClassRanking(Long classGradeId, Long examId) {
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", examId));

        List<Mark> examMarks = markRepo.findByExamId(examId).stream()
                .filter(m -> m.getStudent().getClassGrade() != null
                        && m.getStudent().getClassGrade().getId().equals(classGradeId))
                .collect(Collectors.toList());

        List<StudentRankDto> result = examMarks.stream().map(m -> {
            Student s = m.getStudent();
            double pct = m.getIsAbsent() ? 0 :
                    Math.round((m.getMarksObtained() / exam.getTotalMarks() * 100) * 10.0) / 10.0;
            String className = s.getClassGrade() != null
                    ? s.getClassGrade().getGradeName() + "-" + s.getClassGrade().getSection() : "N/A";
            return StudentRankDto.builder()
                    .studentId(s.getId())
                    .fullName(s.getUser() != null ? s.getUser().getFullName() : s.getRollNumber())
                    .rollNumber(s.getRollNumber())
                    .className(className)
                    .totalObtained(m.getIsAbsent() ? 0 : m.getMarksObtained())
                    .totalMax((double) exam.getTotalMarks())
                    .percentage(pct)
                    .overallGrade(m.getGrade())
                    .build();
        }).collect(Collectors.toList());

        result.sort(Comparator.comparingDouble(StudentRankDto::getPercentage).reversed());
        AtomicInteger rc = new AtomicInteger(1);
        result.forEach(r -> r.setRank(rc.getAndIncrement()));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Attendance Health for a Class
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns attendance summary for every student in a class, flagging those below 75%.
     */
    public List<AttendanceHealthDto> getAttendanceHealth(Long classGradeId) {
        List<Student> students = studentRepo.findAll().stream()
                .filter(s -> s.getClassGrade() != null && s.getClassGrade().getId().equals(classGradeId))
                .collect(Collectors.toList());

        return students.stream().map(s -> {
            long totalPresent = countForStudent(s.getId(), AttendanceStatus.PRESENT);
            long totalAbsent = countForStudent(s.getId(), AttendanceStatus.ABSENT);
            long totalLate = countForStudent(s.getId(), AttendanceStatus.LATE);
            long total = totalPresent + totalAbsent + totalLate;
            double pct = total > 0
                    ? Math.round(((totalPresent + totalLate) * 100.0 / total) * 10.0) / 10.0 : 0.0;

            return AttendanceHealthDto.builder()
                    .studentId(s.getId())
                    .fullName(s.getUser() != null ? s.getUser().getFullName() : s.getRollNumber())
                    .rollNumber(s.getRollNumber())
                    .totalClasses(total)
                    .daysPresent(totalPresent)
                    .daysAbsent(totalAbsent)
                    .daysLate(totalLate)
                    .attendancePct(pct)
                    .flagged(pct < 75.0)
                    .build();
        }).sorted(Comparator.comparingDouble(AttendanceHealthDto::getAttendancePct))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Subject Analytics for an Exam
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calculates average, highest, lowest, and pass rate for a specific exam.
     */
    public SubjectAnalyticsDto getSubjectAnalytics(Long examId) {
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", examId));

        List<Mark> marks = markRepo.findByExamId(examId).stream()
                .filter(m -> !m.getIsAbsent()).collect(Collectors.toList());

        if (marks.isEmpty()) {
            return SubjectAnalyticsDto.builder()
                    .examId(examId).examTitle(exam.getTitle())
                    .subjectName(exam.getSubject().getName())
                    .totalMarks(exam.getTotalMarks())
                    .classAverage(0.0).highest(0.0).lowest(0.0)
                    .totalStudents(0L).passed(0L).passRate(0.0).build();
        }

        DoubleSummaryStatistics stats = marks.stream()
                .mapToDouble(Mark::getMarksObtained).summaryStatistics();
        long passed = marks.stream()
                .filter(m -> m.getMarksObtained() >= exam.getPassingMarks()).count();
        double avg = Math.round(stats.getAverage() * 10.0) / 10.0;
        double passRate = Math.round(passed * 100.0 / marks.size() * 10.0) / 10.0;

        return SubjectAnalyticsDto.builder()
                .examId(examId)
                .examTitle(exam.getTitle())
                .subjectName(exam.getSubject().getName())
                .totalMarks(exam.getTotalMarks())
                .classAverage(avg)
                .highest(stats.getMax())
                .lowest(stats.getMin())
                .totalStudents((long) marks.size())
                .passed(passed)
                .passRate(passRate)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Student Performance Trend
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns exam-by-exam performance for a student (optionally filtered by subject).
     * Perfect for rendering line charts in the frontend.
     */
    public List<StudentTrendDto> getStudentTrend(Long studentId, Long subjectId) {
        List<Mark> marks = markRepo.findByStudentId(studentId);

        return marks.stream()
                .filter(m -> subjectId == null || m.getExam().getSubject().getId().equals(subjectId))
                .filter(m -> m.getExam().getIsPublished())
                .sorted(Comparator.comparing(m -> m.getExam().getExamDate()))
                .map(m -> {
                    double pct = m.getIsAbsent() ? 0 :
                            Math.round((m.getMarksObtained() / m.getExam().getTotalMarks() * 100) * 10.0) / 10.0;
                    return StudentTrendDto.builder()
                            .examId(m.getExam().getId())
                            .examTitle(m.getExam().getTitle())
                            .examType(m.getExam().getExamType())
                            .examDate(m.getExam().getExamDate())
                            .subjectName(m.getExam().getSubject().getName())
                            .marksObtained(m.getMarksObtained())
                            .totalMarks(m.getExam().getTotalMarks())
                            .percentage(pct)
                            .grade(m.getGrade())
                            .isAbsent(m.getIsAbsent())
                            .build();
                }).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. At-Risk Students in a Class
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Identifies students who are at risk based on:
     * - Low attendance (< 75%)
     * - Low academic performance (< 40%)
     * - HIGH risk = both conditions | MEDIUM risk = one condition
     */
    public List<AtRiskStudentDto> getAtRiskStudents(Long classGradeId) {
        List<Student> students = studentRepo.findAll().stream()
                .filter(s -> s.getClassGrade() != null && s.getClassGrade().getId().equals(classGradeId))
                .collect(Collectors.toList());

        List<AtRiskStudentDto> result = new ArrayList<>();

        for (Student s : students) {
            // Attendance pct
            long totalP = countForStudent(s.getId(), AttendanceStatus.PRESENT);
            long totalA = countForStudent(s.getId(), AttendanceStatus.ABSENT);
            long totalL = countForStudent(s.getId(), AttendanceStatus.LATE);
            long total = totalP + totalA + totalL;
            double attPct = total > 0 ? (totalP + totalL) * 100.0 / total : 100.0;

            // Academic pct
            List<Mark> marks = markRepo.findByStudentId(s.getId());
            double obtained = marks.stream().filter(m -> !m.getIsAbsent()).mapToDouble(Mark::getMarksObtained).sum();
            double maxM = marks.stream().filter(m -> !m.getIsAbsent()).mapToDouble(m -> m.getExam().getTotalMarks()).sum();
            double acadPct = maxM > 0 ? obtained / maxM * 100 : 100.0;

            boolean lowAtt = attPct < 75;
            boolean lowAcad = acadPct < 40;

            if (!lowAtt && !lowAcad) continue;  // skip safe students

            String className = s.getClassGrade() != null
                    ? s.getClassGrade().getGradeName() + "-" + s.getClassGrade().getSection() : "N/A";

            result.add(AtRiskStudentDto.builder()
                    .studentId(s.getId())
                    .fullName(s.getUser() != null ? s.getUser().getFullName() : s.getRollNumber())
                    .rollNumber(s.getRollNumber())
                    .className(className)
                    .attendancePct(Math.round(attPct * 10.0) / 10.0)
                    .academicPercentage(Math.round(acadPct * 10.0) / 10.0)
                    .lowAttendance(lowAtt)
                    .lowPerformance(lowAcad)
                    .riskLevel(lowAtt && lowAcad ? "HIGH" : "MEDIUM")
                    .build());
        }

        // HIGH risk first, then MEDIUM
        result.sort(Comparator.comparing(AtRiskStudentDto::getRiskLevel));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private long countForStudent(Long studentId, AttendanceStatus status) {
        // Count across ALL subjects for this student
        return attendanceRepo.findAll().stream()
                .filter(a -> a.getStudent().getId().equals(studentId) && a.getStatus() == status)
                .count();
    }

    private String gradeFromPct(double pct) {
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B+";
        if (pct >= 60) return "B";
        if (pct >= 50) return "C";
        if (pct >= 35) return "D";
        return "F";
    }
}
