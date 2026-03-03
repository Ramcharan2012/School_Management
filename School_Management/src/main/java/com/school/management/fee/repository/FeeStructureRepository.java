package com.school.management.fee.repository;

import com.school.management.fee.entity.FeeStructure;
import com.school.management.common.enums.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    List<FeeStructure> findByAcademicYearId(Long academicYearId);

    List<FeeStructure> findByClassGradeIdAndAcademicYearId(Long classGradeId, Long academicYearId);

    List<FeeStructure> findByClassGradeIdIsNullAndAcademicYearId(Long academicYearId);
}
