package com.school.management.academic.repository;

import com.school.management.academic.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    Optional<AcademicYear> findByIsActiveTrue();

    boolean existsByYearLabel(String yearLabel);

    Optional<AcademicYear> findByYearLabel(String yearLabel);
}
