package com.school.management.teacher.repository;

import com.school.management.teacher.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUserId(Long userId);

    Optional<Teacher> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    @Query("SELECT t FROM Teacher t WHERE t.department.id = :deptId AND t.isActive = true")
    Page<Teacher> findByDepartmentId(Long deptId, Pageable pageable);

    @Query("SELECT t FROM Teacher t JOIN t.user u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(t.employeeId) LIKE LOWER(CONCAT('%',:search,'%'))")
    Page<Teacher> searchTeachers(String search, Pageable pageable);
}
