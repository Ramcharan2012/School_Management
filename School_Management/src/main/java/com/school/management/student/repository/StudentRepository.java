package com.school.management.student.repository;

import com.school.management.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByRollNumber(String rollNumber);

    boolean existsByRollNumber(String rollNumber);

    @Query("SELECT s FROM Student s WHERE s.classGrade.id = :classGradeId AND s.isActive = true")
    Page<Student> findByClassGradeId(Long classGradeId, Pageable pageable);

    @Query("SELECT s FROM Student s JOIN s.user u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(s.rollNumber) LIKE LOWER(CONCAT('%',:search,'%'))")
    Page<Student> searchStudents(String search, Pageable pageable);

    long countByClassGradeId(Long classGradeId);
}
