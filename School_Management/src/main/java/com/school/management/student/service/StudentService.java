package com.school.management.student.service;

import com.school.management.academic.entity.ClassGrade;
import com.school.management.academic.repository.ClassGradeRepository;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.response.PageResponse;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepo;
    private final ClassGradeRepository classGradeRepo;

    public PageResponse<Student> getAllStudents(Pageable pageable) {
        return PageResponse.of(studentRepo.findAll(pageable));
    }

    public PageResponse<Student> searchStudents(String search, Pageable pageable) {
        return PageResponse.of(studentRepo.searchStudents(search, pageable));
    }

    public PageResponse<Student> getStudentsByClass(Long classGradeId, Pageable pageable) {
        return PageResponse.of(studentRepo.findByClassGradeId(classGradeId, pageable));
    }

    public Student getStudentById(Long id) {
        return studentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    public Student getStudentByUserId(Long userId) {
        return studentRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", String.valueOf(userId)));
    }

    @Transactional
    public Student transferStudentToClass(Long studentId, Long newClassGradeId) {
        Student student = getStudentById(studentId);
        ClassGrade newClass = classGradeRepo.findById(newClassGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", newClassGradeId));
        long currentCount = studentRepo.countByClassGradeId(newClassGradeId);
        if (newClass.getCapacity() != null && currentCount >= newClass.getCapacity()) {
            throw new com.school.management.common.exception.BadRequestException(
                    "Class " + newClass.getDisplayName() + " is at full capacity (" + newClass.getCapacity()
                            + " students).");
        }
        student.setClassGrade(newClass);
        return studentRepo.save(student);
    }

    @Transactional
    public Student updateStudentProfile(Long id, String address, String parentPhone, String emergencyContact) {
        Student student = getStudentById(id);
        if (address != null)
            student.setAddress(address);
        if (parentPhone != null)
            student.setParentPhone(parentPhone);
        if (emergencyContact != null)
            student.setEmergencyContact(emergencyContact);
        return studentRepo.save(student);
    }

    @Transactional
    public void deactivateStudent(Long id) {
        Student student = getStudentById(id);
        student.setIsActive(false);
        student.getUser().setStatus(com.school.management.common.enums.UserStatus.INACTIVE);
        studentRepo.save(student);
    }
}
