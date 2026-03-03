package com.school.management.academic.service;

import com.school.management.academic.entity.*;
import com.school.management.academic.repository.*;
import com.school.management.common.exception.BadRequestException;
import com.school.management.common.exception.DuplicateResourceException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.response.PageResponse;
import com.school.management.teacher.entity.Teacher;
import com.school.management.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicService {

    private final DepartmentRepository departmentRepo;
    private final AcademicYearRepository academicYearRepo;
    private final ClassGradeRepository classGradeRepo;
    private final SubjectRepository subjectRepo;
    private final SubjectAssignmentRepository assignmentRepo;
    private final TeacherRepository teacherRepo;

    // ── Departments ───────────────────────────────────────────────────────────

    @Transactional
    public Department createDepartment(String name, String code, String description) {
        if (departmentRepo.existsByCode(code.toUpperCase()))
            throw new DuplicateResourceException("Department code already exists: " + code);
        if (departmentRepo.existsByName(name))
            throw new DuplicateResourceException("Department name already exists: " + name);
        return departmentRepo
                .save(Department.builder().name(name).code(code.toUpperCase()).description(description).build());
    }

    public List<Department> getAllDepartments() {
        return departmentRepo.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    @Transactional
    public Department updateDepartment(Long id, String name, String description) {
        Department dept = getDepartmentById(id);
        dept.setName(name);
        dept.setDescription(description);
        return departmentRepo.save(dept);
    }

    // ── Academic Years ────────────────────────────────────────────────────────

    @Transactional
    public AcademicYear createAcademicYear(String yearLabel, java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        if (academicYearRepo.existsByYearLabel(yearLabel))
            throw new DuplicateResourceException("Academic year already exists: " + yearLabel);
        return academicYearRepo.save(AcademicYear.builder().yearLabel(yearLabel).startDate(startDate).endDate(endDate)
                .isActive(false).build());
    }

    @Transactional
    public AcademicYear setActiveAcademicYear(Long id) {
        // Deactivate current active year
        academicYearRepo.findByIsActiveTrue().ifPresent(ay -> {
            ay.setIsActive(false);
            academicYearRepo.save(ay);
        });
        AcademicYear year = academicYearRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", id));
        year.setIsActive(true);
        return academicYearRepo.save(year);
    }

    public AcademicYear getActiveAcademicYear() {
        return academicYearRepo.findByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active academic year found. Please set one."));
    }

    public List<AcademicYear> getAllAcademicYears() {
        return academicYearRepo.findAll();
    }

    // ── Classes / Grades ──────────────────────────────────────────────────────

    @Transactional
    public ClassGrade createClassGrade(String gradeName, String section, Integer capacity, String roomNumber,
            Long academicYearId) {
        if (classGradeRepo.existsByGradeNameAndSectionAndAcademicYearId(gradeName, section, academicYearId))
            throw new DuplicateResourceException(
                    "Class " + gradeName + "-" + section + " already exists in this academic year.");
        AcademicYear year = academicYearRepo.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", academicYearId));
        return classGradeRepo.save(ClassGrade.builder().gradeName(gradeName).section(section).capacity(capacity)
                .roomNumber(roomNumber).academicYear(year).build());
    }

    public List<ClassGrade> getClassesByAcademicYear(Long academicYearId) {
        return classGradeRepo.findByAcademicYearId(academicYearId);
    }

    public PageResponse<ClassGrade> getClassesPaged(Long academicYearId, Pageable pageable) {
        return PageResponse.of(classGradeRepo.findByAcademicYearId(academicYearId, pageable));
    }

    @Transactional
    public ClassGrade assignClassTeacher(Long classGradeId, Long teacherId) {
        ClassGrade classGrade = classGradeRepo.findById(classGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", classGradeId));
        Teacher teacher = teacherRepo.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));
        classGrade.setClassTeacher(teacher);
        return classGradeRepo.save(classGrade);
    }

    // ── Subjects ──────────────────────────────────────────────────────────────

    @Transactional
    public Subject createSubject(String name, String code, String description, Integer creditHours, Long departmentId) {
        if (subjectRepo.existsByCode(code.toUpperCase()))
            throw new DuplicateResourceException("Subject code already exists: " + code);
        Department dept = departmentId != null ? getDepartmentById(departmentId) : null;
        return subjectRepo.save(Subject.builder().name(name).code(code.toUpperCase()).description(description)
                .creditHours(creditHours).department(dept).build());
    }

    public PageResponse<Subject> getAllSubjects(Pageable pageable) {
        return PageResponse.of(subjectRepo.findAll(pageable));
    }

    public Subject getSubjectById(Long id) {
        return subjectRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subject", id));
    }

    // ── Subject Assignments ───────────────────────────────────────────────────

    @Transactional
    public SubjectAssignment assignTeacherToSubject(Long teacherId, Long subjectId, Long classGradeId) {
        if (assignmentRepo.existsByTeacherIdAndSubjectIdAndClassGradeId(teacherId, subjectId, classGradeId))
            throw new DuplicateResourceException("This teacher is already assigned to this subject in this class.");
        Teacher teacher = teacherRepo.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));
        Subject subject = getSubjectById(subjectId);
        ClassGrade classGrade = classGradeRepo.findById(classGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", classGradeId));
        return assignmentRepo
                .save(SubjectAssignment.builder().teacher(teacher).subject(subject).classGrade(classGrade).build());
    }

    public List<SubjectAssignment> getAssignmentsByClass(Long classGradeId) {
        return assignmentRepo.findActiveByClassGradeId(classGradeId);
    }

    public List<SubjectAssignment> getAssignmentsByTeacher(Long teacherId) {
        return assignmentRepo.findByTeacherId(teacherId);
    }
}
