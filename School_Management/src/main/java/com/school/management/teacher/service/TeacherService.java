package com.school.management.teacher.service;

import com.school.management.academic.entity.Department;
import com.school.management.academic.repository.DepartmentRepository;
import com.school.management.common.enums.Role;
import com.school.management.common.enums.UserStatus;
import com.school.management.common.exception.DuplicateResourceException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.response.PageResponse;
import com.school.management.common.util.IdGeneratorUtil;
import com.school.management.teacher.entity.Teacher;
import com.school.management.teacher.repository.TeacherRepository;
import com.school.management.user.entity.User;
import com.school.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepo;
    private final UserRepository userRepo;
    private final DepartmentRepository departmentRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, Object> createTeacher(String firstName, String lastName, String email,
            String phoneNumber, String qualification, String designation,
            String specialization, Integer experienceYears, Long departmentId) {

        if (userRepo.existsByEmail(email))
            throw new DuplicateResourceException("A user with email " + email + " already exists.");

        Department dept = departmentId != null ? departmentRepo.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId)) : null;

        String username = generateUsername(firstName, lastName);
        String tempPassword = firstName + "@Teacher123";

        User user = User.builder()
                .firstName(firstName).lastName(lastName)
                .username(username).email(email).password(passwordEncoder.encode(tempPassword))
                .phoneNumber(phoneNumber).role(Role.TEACHER).status(UserStatus.ACTIVE).isFirstLogin(true)
                .build();
        User savedUser = userRepo.save(user);

        Teacher teacher = Teacher.builder()
                .user(savedUser).department(dept)
                .employeeId(IdGeneratorUtil.generateTeacherEmployeeId())
                .qualification(qualification).designation(designation)
                .specialization(specialization).experienceYears(experienceYears)
                .joiningDate(LocalDate.now()).isActive(true)
                .build();
        Teacher savedTeacher = teacherRepo.save(teacher);

        log.info("Teacher created: {} / temp password: {} [remove in prod]", username, tempPassword);
        return Map.of("teacher", savedTeacher, "username", username, "temporaryPassword", tempPassword);
    }

    public PageResponse<Teacher> getAllTeachers(Pageable pageable) {
        return PageResponse.of(teacherRepo.findAll(pageable));
    }

    public PageResponse<Teacher> searchTeachers(String search, Pageable pageable) {
        return PageResponse.of(teacherRepo.searchTeachers(search, pageable));
    }

    public Teacher getTeacherById(Long id) {
        return teacherRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Teacher", id));
    }

    @Transactional
    public Teacher updateTeacher(Long id, String qualification, String designation, String specialization,
            Integer experienceYears, Long departmentId) {
        Teacher teacher = getTeacherById(id);
        teacher.setQualification(qualification);
        teacher.setDesignation(designation);
        teacher.setSpecialization(specialization);
        teacher.setExperienceYears(experienceYears);
        if (departmentId != null) {
            teacher.setDepartment(departmentRepo.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId)));
        }
        return teacherRepo.save(teacher);
    }

    @Transactional
    public void deactivateTeacher(Long id) {
        Teacher teacher = getTeacherById(id);
        teacher.setIsActive(false);
        teacher.getUser().setStatus(UserStatus.INACTIVE);
        teacherRepo.save(teacher);
    }

    private String generateUsername(String firstName, String lastName) {
        String base = (firstName.toLowerCase() + "." + lastName.toLowerCase()).replaceAll("\\s+", "");
        String username = base;
        int counter = 1;
        while (userRepo.existsByUsername(username)) {
            username = base + counter++;
        }
        return username;
    }
}
