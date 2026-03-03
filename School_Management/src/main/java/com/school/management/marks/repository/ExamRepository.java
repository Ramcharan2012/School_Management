package com.school.management.marks.repository;

import com.school.management.marks.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    Page<Exam> findByClassGradeId(Long classGradeId, Pageable pageable);

    List<Exam> findBySubjectIdAndClassGradeId(Long subjectId, Long classGradeId);

    Page<Exam> findByClassGradeIdAndIsPublishedTrue(Long classGradeId, Pageable pageable);
}
