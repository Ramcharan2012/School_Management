package com.school.management.staff.repository;

import com.school.management.common.enums.StaffCategory;
import com.school.management.staff.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByStaffId(String staffId);

    Optional<Staff> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Page<Staff> findByIsActiveTrue(Pageable pageable);

    Page<Staff> findByStaffCategoryAndIsActiveTrue(StaffCategory category, Pageable pageable);

    Page<Staff> findByDepartmentIdAndIsActiveTrue(Long departmentId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.isActive = true")
    long countActiveStaff();

    List<Staff> findByIsActiveTrue();
}
