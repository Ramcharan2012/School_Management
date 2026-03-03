package com.school.management.marks.service;

import com.school.management.academic.entity.ClassGrade;
import com.school.management.academic.entity.Subject;
import com.school.management.academic.repository.ClassGradeRepository;
import com.school.management.academic.repository.SubjectRepository;
import com.school.management.common.enums.ExamType;
import com.school.management.common.exception.BadRequestException;
import com.school.management.common.exception.DuplicateResourceException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.response.PageResponse;
import com.school.management.marks.entity.Exam;
import com.school.management.marks.entity.Mark;
import com.school.management.marks.repository.ExamRepository;
import com.school.management.marks.repository.MarkRepository;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
import com.school.management.teacher.entity.Teacher;
import com.school.management.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarksService {

    private final ExamRepository examRepo;
    private final MarkRepository markRepo;
    private final SubjectRepository subjectRepo;
    private final ClassGradeRepository classGradeRepo;
    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;

    // ── Exam Management ───────────────────────────────────────────────────────

    @Transactional
    public Exam createExam(String title, ExamType type, LocalDate date, Integer totalMarks,
            Integer passingMarks, Integer duration, String description,
            Long subjectId, Long classGradeId) {
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        ClassGrade classGrade = classGradeRepo.findById(classGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", classGradeId));
        return examRepo.save(Exam.builder()
                .title(title).examType(type).examDate(date).totalMarks(totalMarks)
                .passingMarks(passingMarks).durationMinutes(duration).description(description)
                .subject(subject).classGrade(classGrade).isPublished(false)
                .build());
    }

    public PageResponse<Exam> getExamsByClass(Long classGradeId, boolean publishedOnly, Pageable pageable) {
        if (publishedOnly)
            return PageResponse.of(examRepo.findByClassGradeIdAndIsPublishedTrue(classGradeId, pageable));
        return PageResponse.of(examRepo.findByClassGradeId(classGradeId, pageable));
    }

    @Transactional
    public Exam publishExamResults(Long examId) {
        Exam exam = examRepo.findById(examId).orElseThrow(() -> new ResourceNotFoundException("Exam", examId));
        exam.setIsPublished(true);
        return examRepo.save(exam);
    }

    // ── Mark Entry (Teacher) ──────────────────────────────────────────────────

    @Transactional
    public Mark enterMark(Long studentId, Long examId, Long teacherId, Double marksObtained,
            Boolean isAbsent, String remarks) {
        markRepo.findByStudentIdAndExamId(studentId, examId)
                .ifPresent(m -> {
                    throw new DuplicateResourceException("Marks already entered for this student/exam.");
                });

        Exam exam = examRepo.findById(examId).orElseThrow(() -> new ResourceNotFoundException("Exam", examId));
        if (!isAbsent && (marksObtained < 0 || marksObtained > exam.getTotalMarks())) {
            throw new BadRequestException("Marks must be between 0 and " + exam.getTotalMarks());
        }

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Teacher teacher = teacherRepo.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));

        String grade = isAbsent ? "AB" : calculateGrade(marksObtained, exam.getTotalMarks());

        return markRepo.save(Mark.builder()
                .student(student).exam(exam).enteredBy(teacher)
                .marksObtained(isAbsent ? 0.0 : marksObtained)
                .isAbsent(isAbsent).grade(grade).remarks(remarks)
                .build());
    }

    @Transactional
    public Mark updateMark(Long markId, Double newMarks, String remarks) {
        Mark mark = markRepo.findById(markId).orElseThrow(() -> new ResourceNotFoundException("Mark", markId));
        if (newMarks < 0 || newMarks > mark.getExam().getTotalMarks())
            throw new BadRequestException("Marks must be between 0 and " + mark.getExam().getTotalMarks());
        mark.setMarksObtained(newMarks);
        mark.setGrade(calculateGrade(newMarks, mark.getExam().getTotalMarks()));
        mark.setRemarks(remarks);
        return markRepo.save(mark);
    }

    // ── View Marks ────────────────────────────────────────────────────────────

    public PageResponse<Mark> getStudentMarks(Long studentId, Pageable pageable) {
        return PageResponse.of(markRepo.findByStudentId(studentId, pageable));
    }

    public List<Mark> getExamResults(Long examId) {
        return markRepo.findByExamId(examId);
    }

    public Map<String, Object> getStudentReportCard(Long studentId, Long classGradeId) {
        List<Mark> marks = markRepo.findByStudentId(studentId);
        double totalObtained = marks.stream().filter(m -> !m.getIsAbsent()).mapToDouble(Mark::getMarksObtained).sum();
        double totalMax = marks.stream().filter(m -> !m.getIsAbsent()).mapToDouble(m -> m.getExam().getTotalMarks())
                .sum();
        double percentage = totalMax > 0 ? Math.round((totalObtained / totalMax * 100) * 10.0) / 10.0 : 0.0;

        return Map.of("studentId", studentId, "marks", marks,
                "totalMarksObtained", totalObtained, "totalMaxMarks", totalMax,
                "percentage", percentage, "overallGrade", calculateGrade(percentage, 100));
    }

    // ── Grade Calculation ─────────────────────────────────────────────────────

    private String calculateGrade(double obtained, double total) {
        double pct = (obtained / total) * 100;
        if (pct >= 90)
            return "A+";
        if (pct >= 80)
            return "A";
        if (pct >= 70)
            return "B+";
        if (pct >= 60)
            return "B";
        if (pct >= 50)
            return "C";
        if (pct >= 35)
            return "D";
        return "F";
    }
}
