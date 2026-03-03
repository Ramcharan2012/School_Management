package com.school.management.marks.repository;

import com.school.management.marks.entity.Mark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {
    Optional<Mark> findByStudentIdAndExamId(Long studentId, Long examId);

    List<Mark> findByStudentId(Long studentId);

    List<Mark> findByExamId(Long examId);

    Page<Mark> findByStudentId(Long studentId, Pageable pageable);

    @Query("SELECT m FROM Mark m WHERE m.student.id = :studentId AND m.exam.subject.id = :subjectId")
    List<Mark> findByStudentIdAndSubjectId(Long studentId, Long subjectId);

    @Query("SELECT AVG(m.marksObtained) FROM Mark m WHERE m.exam.id = :examId")
    Double findAverageMarksByExamId(Long examId);
}
