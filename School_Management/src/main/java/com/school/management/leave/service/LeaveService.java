package com.school.management.leave.service;

import com.school.management.common.enums.LeaveStatus;
import com.school.management.common.exception.BadRequestException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.response.PageResponse;
import com.school.management.leave.entity.LeaveRequest;
import com.school.management.leave.repository.LeaveRequestRepository;
import com.school.management.user.entity.User;
import com.school.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRepo;
    private final UserRepository userRepo;

    @Transactional
    public LeaveRequest applyLeave(Long applicantId, String subject, String reason,
            LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isBefore(LocalDate.now()))
            throw new BadRequestException("Leave start date cannot be in the past.");
        if (toDate.isBefore(fromDate))
            throw new BadRequestException("End date must be on or after start date.");

        User applicant = userRepo.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", applicantId));
        int days = (int) ChronoUnit.DAYS.between(fromDate, toDate) + 1;

        return leaveRepo.save(LeaveRequest.builder()
                .applicant(applicant).subject(subject).reason(reason)
                .fromDate(fromDate).toDate(toDate).totalDays(days)
                .status(LeaveStatus.PENDING)
                .build());
    }

    @Transactional
    public LeaveRequest reviewLeave(Long leaveId, LeaveStatus status,
            String adminRemarks, Long reviewerUserId) {
        LeaveRequest leave = leaveRepo.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", leaveId));
        if (leave.getStatus() != LeaveStatus.PENDING)
            throw new BadRequestException("Leave is already " + leave.getStatus());

        User reviewer = userRepo.findById(reviewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", reviewerUserId));
        leave.setStatus(status);
        leave.setAdminRemarks(adminRemarks);
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(LocalDateTime.now());
        return leaveRepo.save(leave);
    }

    public PageResponse<LeaveRequest> getMyLeaves(Long userId, Pageable pageable) {
        return PageResponse.of(leaveRepo.findByApplicantId(userId, pageable));
    }

    public PageResponse<LeaveRequest> getPendingLeaves(Pageable pageable) {
        return PageResponse.of(leaveRepo.findByStatus(LeaveStatus.PENDING, pageable));
    }

    public PageResponse<LeaveRequest> getAllLeaves(Pageable pageable) {
        return PageResponse.of(leaveRepo.findAll(pageable));
    }
}
