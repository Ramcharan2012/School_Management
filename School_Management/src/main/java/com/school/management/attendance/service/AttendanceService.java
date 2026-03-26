package com.school.management.attendance.service;

import com.school.management.academic.entity.Subject;
import com.school.management.academic.repository.SubjectRepository;
import com.school.management.attendance.entity.Attendance;
import com.school.management.attendance.repository.AttendanceRepository;
import com.school.management.common.enums.AttendanceStatus;
import com.school.management.common.exception.BadRequestException;
import com.school.management.common.exception.DuplicateResourceException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.service.EmailService;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
import com.school.management.teacher.entity.Teacher;
import com.school.management.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {

        private final AttendanceRepository attendanceRepo;
        private final StudentRepository studentRepo;
        private final SubjectRepository subjectRepo;
        private final TeacherRepository teacherRepo;
        private final EmailService emailService;

        /**
         * Mark attendance for a single student. Teacher provides: studentId, subjectId,
         * date, status.
         */
        @Transactional
        public Attendance markAttendance(Long studentId, Long subjectId, Long teacherId,
                        LocalDate date, AttendanceStatus status, String remarks) {
                if (date.isAfter(LocalDate.now()))
                        throw new BadRequestException("Cannot mark attendance for a future date.");

                attendanceRepo.findByStudentIdAndSubjectIdAndAttendanceDate(studentId, subjectId, date)
                                .ifPresent(a -> {
                                        throw new DuplicateResourceException(
                                                        "Attendance already marked for this student/subject/date.");
                                });

                Student student = studentRepo.findById(studentId)
                                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
                Subject subject = subjectRepo.findById(subjectId)
                                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
                Teacher teacher = teacherRepo.findById(teacherId)
                                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));

                Attendance saved = attendanceRepo.save(Attendance.builder()
                                .student(student).subject(subject).markedBy(teacher)
                                .attendanceDate(date).status(status).remarks(remarks)
                                .build());

                // Notify parent if student is ABSENT
                if (status == AttendanceStatus.ABSENT && student.getParentEmail() != null) {
                        emailService.sendAbsenceNotificationEmail(
                                        student.getParentEmail(),
                                        student.getParentName() != null ? student.getParentName() : "Parent/Guardian",
                                        student.getUser() != null ? student.getUser().getFullName()
                                                        : student.getRollNumber(),
                                        subject.getName(),
                                        date);
                }

                return saved;
        }

        @Transactional
        public Attendance updateAttendance(Long attendanceId, AttendanceStatus newStatus, String remarks) {
                Attendance attendance = attendanceRepo.findById(attendanceId)
                                .orElseThrow(() -> new ResourceNotFoundException("Attendance", attendanceId));
                attendance.setStatus(newStatus);
                attendance.setRemarks(remarks);
                return attendanceRepo.save(attendance);
        }

        public List<Attendance> getStudentAttendanceBySubject(Long studentId, Long subjectId) {
                return attendanceRepo.findByStudentIdAndSubjectIdOrderByAttendanceDateDesc(studentId, subjectId);
        }

        public List<Attendance> getAttendanceForSubjectOnDate(Long subjectId, LocalDate date) {
                return attendanceRepo.findBySubjectIdAndDate(subjectId, date);
        }

        /**
         * Attendance summary for a student in a subject: total, present, absent,
         * percentage.
         */
        public Map<String, Object> getAttendanceSummary(Long studentId, Long subjectId) {
                long total = attendanceRepo.countByStudentIdAndSubjectIdAndStatus(studentId, subjectId,
                                AttendanceStatus.PRESENT)
                                + attendanceRepo.countByStudentIdAndSubjectIdAndStatus(studentId, subjectId,
                                                AttendanceStatus.ABSENT)
                                + attendanceRepo.countByStudentIdAndSubjectIdAndStatus(studentId, subjectId,
                                                AttendanceStatus.LATE);
                long present = attendanceRepo.countByStudentIdAndSubjectIdAndStatus(studentId, subjectId,
                                AttendanceStatus.PRESENT);
                long late = attendanceRepo.countByStudentIdAndSubjectIdAndStatus(studentId, subjectId,
                                AttendanceStatus.LATE);
                long absent = attendanceRepo.countByStudentIdAndSubjectIdAndStatus(studentId, subjectId,
                                AttendanceStatus.ABSENT);
                double pct = total > 0 ? Math.round(((present + late) * 100.0 / total) * 10.0) / 10.0 : 0.0;

                return Map.of("studentId", studentId, "subjectId", subjectId,
                                "totalClasses", total, "present", present, "late", late,
                                "absent", absent, "attendancePercentage", pct,
                                "status", pct >= 75 ? "SATISFACTORY" : "LOW_ATTENDANCE");
        }
}
