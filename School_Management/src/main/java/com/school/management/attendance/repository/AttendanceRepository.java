package com.school.management.attendance.repository;

import com.school.management.attendance.entity.Attendance;
import com.school.management.common.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentIdAndSubjectIdAndAttendanceDate(
            Long studentId, Long subjectId, LocalDate date);

    List<Attendance> findByStudentIdAndSubjectIdOrderByAttendanceDateDesc(Long studentId, Long subjectId);

    Page<Attendance> findByStudentIdAndAttendanceDateBetween(
            Long studentId, LocalDate from, LocalDate to, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId " +
            "AND a.subject.id = :subjectId AND a.status = :status")
    long countByStudentIdAndSubjectIdAndStatus(Long studentId, Long subjectId, AttendanceStatus status);

    @Query("SELECT a FROM Attendance a WHERE a.subject.id = :subjectId " +
            "AND a.attendanceDate = :date")
    List<Attendance> findBySubjectIdAndDate(Long subjectId, LocalDate date);
}
