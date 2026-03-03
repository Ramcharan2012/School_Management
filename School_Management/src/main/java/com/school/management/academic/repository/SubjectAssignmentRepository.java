package com.school.management.academic.repository;

import com.school.management.academic.entity.SubjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubjectAssignmentRepository extends JpaRepository<SubjectAssignment, Long> {
    List<SubjectAssignment> findByClassGradeId(Long classGradeId);

    List<SubjectAssignment> findByTeacherId(Long teacherId);

    boolean existsByTeacherIdAndSubjectIdAndClassGradeId(Long teacherId, Long subjectId, Long classGradeId);

    @Query("SELECT sa FROM SubjectAssignment sa WHERE sa.classGrade.id = :classGradeId AND sa.isActive = true")
    List<SubjectAssignment> findActiveByClassGradeId(Long classGradeId);
}
