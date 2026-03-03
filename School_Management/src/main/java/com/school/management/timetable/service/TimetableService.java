package com.school.management.timetable.service;

import com.school.management.academic.entity.ClassGrade;
import com.school.management.academic.entity.Subject;
import com.school.management.academic.repository.ClassGradeRepository;
import com.school.management.academic.repository.SubjectRepository;
import com.school.management.common.exception.DuplicateResourceException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.teacher.entity.Teacher;
import com.school.management.teacher.repository.TeacherRepository;
import com.school.management.timetable.entity.Timetable;
import com.school.management.timetable.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepo;
    private final ClassGradeRepository classGradeRepo;
    private final SubjectRepository subjectRepo;
    private final TeacherRepository teacherRepo;

    @Transactional
    public Timetable createEntry(Long classGradeId, Long subjectId, Long teacherId,
            DayOfWeek day, LocalTime startTime, LocalTime endTime, String roomNumber) {
        if (timetableRepo.existsByClassGradeIdAndDayOfWeekAndStartTime(classGradeId, day, startTime))
            throw new DuplicateResourceException("A class is already scheduled at this time slot.");

        ClassGrade cg = classGradeRepo.findById(classGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", classGradeId));
        Subject sub = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        Teacher teacher = teacherRepo.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));

        return timetableRepo.save(Timetable.builder()
                .classGrade(cg).subject(sub).teacher(teacher)
                .dayOfWeek(day).startTime(startTime).endTime(endTime)
                .roomNumber(roomNumber).isActive(true)
                .build());
    }

    public List<Timetable> getTimetableForClass(Long classGradeId) {
        return timetableRepo.findByClassGradeIdOrderByDayOfWeekAscStartTimeAsc(classGradeId);
    }

    public List<Timetable> getTimetableForTeacher(Long teacherId) {
        return timetableRepo.findByTeacherIdOrderByDayOfWeekAscStartTimeAsc(teacherId);
    }

    @Transactional
    public void deleteEntry(Long id) {
        Timetable tt = timetableRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Timetable", id));
        tt.setIsActive(false);
        timetableRepo.save(tt);
    }
}
