package com.school.management.academic.repository;

import com.school.management.academic.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCode(String code);

    boolean existsByCode(String code);

    Page<Subject> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<Subject> findByIsActiveTrue(Pageable pageable);
}
