package com.school.management.staff.service;

import com.school.management.academic.entity.Department;
import com.school.management.academic.repository.DepartmentRepository;
import com.school.management.common.enums.Role;
import com.school.management.common.enums.StaffCategory;
import com.school.management.common.enums.UserStatus;
import com.school.management.common.exception.BadRequestException;
import com.school.management.common.exception.DuplicateResourceException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.response.PageResponse;
import com.school.management.common.service.EmailService;
import com.school.management.common.util.IdGeneratorUtil;
import com.school.management.staff.entity.Staff;
import com.school.management.staff.repository.StaffRepository;
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
public class StaffService {

    private final StaffRepository staffRepo;
    private final UserRepository userRepo;
    private final DepartmentRepository departmentRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> createStaff(String firstName, String lastName, String email,
            String phoneNumber, StaffCategory category, String designation,
            String qualification, Long departmentId) {

        if (userRepo.existsByEmail(email))
            throw new DuplicateResourceException("A user with email " + email + " already exists.");

        Department dept = departmentId != null
                ? departmentRepo.findById(departmentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId))
                : null;

        // Generate unique username like priya.sharma / priya.sharma1
        String username = generateUsername(firstName, lastName);
        String tempPassword = firstName + "@Staff123";

        User user = User.builder()
                .firstName(firstName).lastName(lastName)
                .username(username).email(email)
                .password(passwordEncoder.encode(tempPassword))
                .phoneNumber(phoneNumber)
                .role(Role.STAFF).status(UserStatus.ACTIVE).isFirstLogin(true)
                .build();
        User savedUser = userRepo.save(user);

        Staff staff = Staff.builder()
                .user(savedUser).department(dept)
                .staffId(IdGeneratorUtil.generateStaffId())
                .staffCategory(category)
                .designation(designation)
                .qualification(qualification)
                .joiningDate(LocalDate.now())
                .isActive(true)
                .build();
        Staff savedStaff = staffRepo.save(staff);

        // Send welcome email if email is available
        if (email != null && !email.isBlank()) {
            emailService.sendStaffWelcomeEmail(email, savedUser.getFullName(),
                    username, tempPassword, category.name());
        }

        log.info("Staff created: {} [{}] staffId={}", username, category, savedStaff.getStaffId());
        return Map.of(
                "staff", savedStaff,
                "username", username,
                "temporaryPassword", tempPassword,
                "staffId", savedStaff.getStaffId());
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public PageResponse<Staff> getAllStaff(Pageable pageable) {
        return PageResponse.of(staffRepo.findByIsActiveTrue(pageable));
    }

    public PageResponse<Staff> getStaffByCategory(StaffCategory category, Pageable pageable) {
        return PageResponse.of(staffRepo.findByStaffCategoryAndIsActiveTrue(category, pageable));
    }

    public Staff getStaffById(Long id) {
        return staffRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public Staff updateStaff(Long id, String designation, String qualification,
            StaffCategory category, Long departmentId) {
        Staff staff = getStaffById(id);
        if (designation != null) staff.setDesignation(designation);
        if (qualification != null) staff.setQualification(qualification);
        if (category != null) staff.setStaffCategory(category);
        if (departmentId != null) {
            staff.setDepartment(departmentRepo.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId)));
        }
        return staffRepo.save(staff);
    }

    // ── Deactivate (Soft Delete) ───────────────────────────────────────────────

    @Transactional
    public void deactivateStaff(Long id) {
        Staff staff = getStaffById(id);
        if (!staff.getIsActive())
            throw new BadRequestException("Staff member is already inactive.");
        staff.setIsActive(false);
        staff.getUser().setStatus(UserStatus.INACTIVE);
        staffRepo.save(staff);
        log.info("Staff deactivated: staffId={}", staff.getStaffId());
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public long countActiveStaff() {
        return staffRepo.countActiveStaff();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
