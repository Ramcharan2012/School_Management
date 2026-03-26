package com.school.management.dashboard.service;

import com.school.management.admission.repository.AdmissionRequestRepository;
import com.school.management.common.enums.AdmissionStatus;
import com.school.management.common.enums.Role;
import com.school.management.common.enums.UserStatus;
import com.school.management.fee.repository.FeePaymentRepository;
import com.school.management.leave.repository.LeaveRequestRepository;
import com.school.management.student.repository.StudentRepository;
import com.school.management.teacher.repository.TeacherRepository;
import com.school.management.user.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;
    private final AdmissionRequestRepository admissionRepo;
    private final FeePaymentRepository feePaymentRepo;
    private final LeaveRequestRepository leaveRepo;

    /**
     * Cached for 5 minutes (300s). Cache is evicted by any admission approval,
     * student creation, or fee payment — configured in the controllers
     * via @CacheEvict.
     */
    @Cacheable(value = "dashboard_stats", key = "'global'")
    public DashboardStats getAdminDashboardStats() {
        long totalStudents = studentRepo.count();
        long totalTeachers = teacherRepo.count();
        long activeUsers = userRepo.countByStatusAndRole(UserStatus.ACTIVE, Role.STUDENT)
                + userRepo.countByStatusAndRole(UserStatus.ACTIVE, Role.TEACHER);
        long pendingAdmissions = admissionRepo.countByStatus(AdmissionStatus.PENDING);
        long approvedAdmissions = admissionRepo.countByStatus(AdmissionStatus.APPROVED);
        long rejectedAdmissions = admissionRepo.countByStatus(AdmissionStatus.REJECTED);

        // Fee collection for current month
        YearMonth currentMonth = YearMonth.now();
        BigDecimal monthlyCollection = feePaymentRepo.getTotalPaidBetween(
                currentMonth.atDay(1),
                currentMonth.atEndOfMonth());
        if (monthlyCollection == null)
            monthlyCollection = BigDecimal.ZERO;

        // Fee collection for current year
        BigDecimal yearlyCollection = feePaymentRepo.getTotalPaidBetween(
                LocalDate.now().withDayOfYear(1),
                LocalDate.now());
        if (yearlyCollection == null)
            yearlyCollection = BigDecimal.ZERO;

        long pendingLeaveRequests = leaveRepo.countByStatus(com.school.management.common.enums.LeaveStatus.PENDING);

        return DashboardStats.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .activeUsers(activeUsers)
                .pendingAdmissions(pendingAdmissions)
                .approvedThisYear(approvedAdmissions)
                .rejectedThisYear(rejectedAdmissions)
                .monthlyFeeCollection(monthlyCollection)
                .yearlyFeeCollection(yearlyCollection)
                .pendingLeaveRequests(pendingLeaveRequests)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Data
    @Builder
    public static class DashboardStats {
        private long totalStudents;
        private long totalTeachers;
        private long activeUsers;
        private long pendingAdmissions;
        private long approvedThisYear;
        private long rejectedThisYear;
        private BigDecimal monthlyFeeCollection;
        private BigDecimal yearlyFeeCollection;
        private long pendingLeaveRequests;
        private LocalDateTime generatedAt;
    }
}
