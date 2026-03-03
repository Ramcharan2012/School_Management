package com.school.management.timetable.repository;

import com.school.management.timetable.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByClassGradeIdOrderByDayOfWeekAscStartTimeAsc(Long classGradeId);

    List<Timetable> findByTeacherIdOrderByDayOfWeekAscStartTimeAsc(Long teacherId);

    List<Timetable> findByClassGradeIdAndDayOfWeek(Long classGradeId, DayOfWeek day);

    boolean existsByClassGradeIdAndDayOfWeekAndStartTime(Long classGradeId, DayOfWeek day,
            java.time.LocalTime startTime);
}
