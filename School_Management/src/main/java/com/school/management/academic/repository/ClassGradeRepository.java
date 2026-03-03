package com.school.management.academic.repository;

import com.school.management.academic.entity.ClassGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassGradeRepository extends JpaRepository<ClassGrade, Long> {
    List<ClassGrade> findByAcademicYearId(Long academicYearId);

    Page<ClassGrade> findByAcademicYearId(Long academicYearId, Pageable pageable);

    boolean existsByGradeNameAndSectionAndAcademicYearId(String gradeName, String section, Long academicYearId);
}
