package com.school.management.admission.repository;

import com.school.management.admission.entity.AdmissionRequest;
import com.school.management.common.enums.AdmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdmissionRequestRepository extends JpaRepository<AdmissionRequest, Long> {
    Optional<AdmissionRequest> findByApplicationNumber(String applicationNumber);

    boolean existsByApplicantEmail(String email);

    Page<AdmissionRequest> findByStatus(AdmissionStatus status, Pageable pageable);

    @Query("SELECT a FROM AdmissionRequest a WHERE " +
            "LOWER(a.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(a.applicationNumber) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(a.applicantEmail) LIKE LOWER(CONCAT('%',:search,'%'))")
    Page<AdmissionRequest> searchAdmissions(String search, Pageable pageable);

    long countByStatus(AdmissionStatus status);
}
