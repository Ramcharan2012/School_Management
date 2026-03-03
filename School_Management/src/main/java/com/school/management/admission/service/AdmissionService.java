package com.school.management.admission.service;

import com.school.management.admission.dto.AdmissionFormRequest;
import com.school.management.admission.dto.AdmissionReviewRequest;
import com.school.management.admission.dto.AdmissionResponse;
import com.school.management.admission.entity.AdmissionRequest;
import com.school.management.admission.repository.AdmissionRequestRepository;
import com.school.management.academic.entity.ClassGrade;
import com.school.management.academic.repository.ClassGradeRepository;
import com.school.management.common.enums.AdmissionStatus;
import com.school.management.common.enums.Role;
import com.school.management.common.enums.UserStatus;
import com.school.management.common.exception.BadRequestException;
import com.school.management.common.exception.DuplicateResourceException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.response.PageResponse;
import com.school.management.common.util.IdGeneratorUtil;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
import com.school.management.user.entity.User;
import com.school.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionService {

    private final AdmissionRequestRepository admissionRepo;
    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final ClassGradeRepository classGradeRepo;
    private final PasswordEncoder passwordEncoder;

    // ── Public: Submit Admission Form ─────────────────────────────────────────

    @Transactional
    public AdmissionResponse submitAdmissionForm(AdmissionFormRequest request) {
        if (admissionRepo.existsByApplicantEmail(request.getApplicantEmail())) {
            throw new DuplicateResourceException(
                    "An application already exists for email: " + request.getApplicantEmail());
        }

        AdmissionRequest admission = new AdmissionRequest();
        admission.setFirstName(request.getFirstName());
        admission.setLastName(request.getLastName());
        admission.setApplicantEmail(request.getApplicantEmail());
        admission.setPhoneNumber(request.getPhoneNumber());
        admission.setDateOfBirth(request.getDateOfBirth());
        admission.setGender(request.getGender());
        admission.setAddress(request.getAddress());
        admission.setBloodGroup(request.getBloodGroup());
        admission.setParentName(request.getParentName());
        admission.setParentPhone(request.getParentPhone());
        admission.setParentEmail(request.getParentEmail());
        admission.setParentOccupation(request.getParentOccupation());
        admission.setApplyingForGrade(request.getApplyingForGrade());
        admission.setAcademicYear(request.getAcademicYear());
        admission.setPreviousSchool(request.getPreviousSchool());
        admission.setPreviousClassCompleted(request.getPreviousClassCompleted());
        admission.setPreviousPercentage(request.getPreviousPercentage());
        admission.setAdmissionFee(request.getAdmissionFee());
        admission.setTuitionFeePerMonth(request.getTuitionFeePerMonth());
        admission.setOtherCharges(request.getOtherCharges());
        admission.setFeeConcessionRequested(request.getFeeConcessionRequested());
        admission.setFeeConcessionReason(request.getFeeConcessionReason());
        admission.setStatus(AdmissionStatus.PENDING);
        admission.setApplicationNumber(IdGeneratorUtil.generateApplicationNumber());

        AdmissionRequest saved = admissionRepo.save(admission);
        log.info("Admission form submitted: {}", saved.getApplicationNumber());
        return mapToResponse(saved);
    }

    // ── Admin: Review / Approve / Reject ──────────────────────────────────────

    @Transactional
    public AdmissionResponse reviewAdmission(Long admissionId, AdmissionReviewRequest reviewRequest,
            String reviewerUsername) {
        AdmissionRequest admission = admissionRepo.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("AdmissionRequest", admissionId));

        if (admission.getStatus() != AdmissionStatus.PENDING &&
                admission.getStatus() != AdmissionStatus.UNDER_REVIEW) {
            throw new BadRequestException("Admission is already " + admission.getStatus() + " and cannot be reviewed.");
        }

        admission.setStatus(reviewRequest.getStatus());
        admission.setAdminRemarks(reviewRequest.getAdminRemarks());
        admission.setReviewedBy(reviewerUsername);
        admission.setReviewedAt(LocalDateTime.now());

        // ── On APPROVED: Create User + Student account ─────────────────────────
        if (reviewRequest.getStatus() == AdmissionStatus.APPROVED) {
            if (reviewRequest.getClassGradeId() == null) {
                throw new BadRequestException("classGradeId is required when approving an admission.");
            }
            if (userRepo.existsByEmail(admission.getApplicantEmail())) {
                throw new DuplicateResourceException("A user with this email already exists.");
            }

            ClassGrade classGrade = classGradeRepo.findById(reviewRequest.getClassGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", reviewRequest.getClassGradeId()));

            // 1. Generate username + temporary password
            String username = generateUsername(admission.getFirstName(), admission.getLastName());
            String tempPassword = generateTemporaryPassword(admission);

            // 2. Create User
            User user = User.builder()
                    .firstName(admission.getFirstName())
                    .lastName(admission.getLastName())
                    .username(username)
                    .email(admission.getApplicantEmail())
                    .password(passwordEncoder.encode(tempPassword))
                    .phoneNumber(admission.getPhoneNumber())
                    .gender(admission.getGender())
                    .role(Role.STUDENT)
                    .status(UserStatus.ACTIVE)
                    .isFirstLogin(true)
                    .build();
            User savedUser = userRepo.save(user);

            // 3. Create Student profile
            String rollNumber = IdGeneratorUtil.generateStudentRollNumber();
            Student student = Student.builder()
                    .user(savedUser)
                    .rollNumber(rollNumber)
                    .classGrade(classGrade)
                    .dateOfBirth(admission.getDateOfBirth())
                    .address(admission.getAddress())
                    .bloodGroup(admission.getBloodGroup())
                    .parentName(admission.getParentName())
                    .parentPhone(admission.getParentPhone())
                    .parentEmail(admission.getParentEmail())
                    .parentOccupation(admission.getParentOccupation())
                    .previousSchool(admission.getPreviousSchool())
                    .admissionDate(java.time.LocalDate.now())
                    .isActive(true)
                    .build();
            studentRepo.save(student);

            admission.setGeneratedUserId(savedUser.getId());

            log.info("Student account created: username={}, rollNumber={}, tempPassword={} [LOG ONLY - remove in prod]",
                    username, rollNumber, tempPassword);
        }

        AdmissionRequest updated = admissionRepo.save(admission);
        return mapToResponse(updated);
    }

    // ── Admin: List & Search ──────────────────────────────────────────────────

    public PageResponse<AdmissionResponse> getAllAdmissions(Pageable pageable) {
        Page<AdmissionResponse> page = admissionRepo.findAll(pageable).map(this::mapToResponse);
        return PageResponse.of(page);
    }

    public PageResponse<AdmissionResponse> getAdmissionsByStatus(AdmissionStatus status, Pageable pageable) {
        Page<AdmissionResponse> page = admissionRepo.findByStatus(status, pageable).map(this::mapToResponse);
        return PageResponse.of(page);
    }

    public PageResponse<AdmissionResponse> searchAdmissions(String keyword, Pageable pageable) {
        Page<AdmissionResponse> page = admissionRepo.searchAdmissions(keyword, pageable).map(this::mapToResponse);
        return PageResponse.of(page);
    }

    public AdmissionResponse getByApplicationNumber(String appNumber) {
        AdmissionRequest admission = admissionRepo.findByApplicationNumber(appNumber)
                .orElseThrow(() -> new ResourceNotFoundException("AdmissionRequest", "applicationNumber", appNumber));
        return mapToResponse(admission);
    }

    public Map<String, Long> getAdmissionStats() {
        return Map.of(
                "pending", admissionRepo.countByStatus(AdmissionStatus.PENDING),
                "under_review", admissionRepo.countByStatus(AdmissionStatus.UNDER_REVIEW),
                "approved", admissionRepo.countByStatus(AdmissionStatus.APPROVED),
                "rejected", admissionRepo.countByStatus(AdmissionStatus.REJECTED));
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private String generateUsername(String firstName, String lastName) {
        String base = (firstName.toLowerCase() + "." + lastName.toLowerCase())
                .replaceAll("\\s+", "");
        String username = base;
        int counter = 1;
        while (userRepo.existsByUsername(username)) {
            username = base + counter++;
        }
        return username;
    }

    private String generateTemporaryPassword(AdmissionRequest admission) {
        // Pattern: FirstName@DDMMYYYY — easy to remember, forced change on first login
        String dob = admission.getDateOfBirth() != null
                ? admission.getDateOfBirth().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"))
                : "School@123";
        return admission.getFirstName() + "@" + dob;
    }

    private AdmissionResponse mapToResponse(AdmissionRequest a) {
        return AdmissionResponse.builder()
                .id(a.getId())
                .applicationNumber(a.getApplicationNumber())
                .firstName(a.getFirstName())
                .lastName(a.getLastName())
                .applicantEmail(a.getApplicantEmail())
                .phoneNumber(a.getPhoneNumber())
                .dateOfBirth(a.getDateOfBirth())
                .gender(a.getGender())
                .address(a.getAddress())
                .parentName(a.getParentName())
                .parentPhone(a.getParentPhone())
                .parentEmail(a.getParentEmail())
                .applyingForGrade(a.getApplyingForGrade())
                .academicYear(a.getAcademicYear())
                .previousSchool(a.getPreviousSchool())
                .admissionFee(a.getAdmissionFee())
                .tuitionFeePerMonth(a.getTuitionFeePerMonth())
                .feeConcessionRequested(a.getFeeConcessionRequested())
                .status(a.getStatus())
                .adminRemarks(a.getAdminRemarks())
                .reviewedBy(a.getReviewedBy())
                .reviewedAt(a.getReviewedAt())
                .generatedUserId(a.getGeneratedUserId())
                .submittedAt(a.getCreatedAt())
                .build();
    }
}
