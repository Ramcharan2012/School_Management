package com.school.management.leave.repository;

import com.school.management.common.enums.LeaveStatus;
import com.school.management.leave.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findByApplicantId(Long userId, Pageable pageable);

    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    long countByApplicantIdAndStatus(Long userId, LeaveStatus status);
}
