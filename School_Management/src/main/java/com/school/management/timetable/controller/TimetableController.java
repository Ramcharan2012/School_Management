package com.school.management.timetable.controller;

import com.school.management.common.response.ApiResponse;
import com.school.management.timetable.entity.Timetable;
import com.school.management.timetable.service.TimetableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Timetable", description = "Weekly class timetable management")
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping("/admin/timetable")
    @Operation(summary = "Add a timetable slot (Admin only)")
    public ResponseEntity<ApiResponse<Timetable>> createEntry(@RequestBody TimetableRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Slot added.",
                timetableService.createEntry(req.getClassGradeId(), req.getSubjectId(), req.getTeacherId(),
                        req.getDayOfWeek(), req.getStartTime(), req.getEndTime(), req.getRoomNumber())));
    }

    @GetMapping("/timetable/class/{classGradeId}")
    @Operation(summary = "Get full timetable for a class (Students/Teachers/Admin)")
    public ResponseEntity<ApiResponse<List<Timetable>>> getClassTimetable(@PathVariable Long classGradeId) {
        return ResponseEntity.ok(ApiResponse.success(timetableService.getTimetableForClass(classGradeId)));
    }

    @GetMapping("/timetable/teacher/{teacherId}")
    @Operation(summary = "Get a teacher's weekly schedule")
    public ResponseEntity<ApiResponse<List<Timetable>>> getTeacherTimetable(@PathVariable Long teacherId) {
        return ResponseEntity.ok(ApiResponse.success(timetableService.getTimetableForTeacher(teacherId)));
    }

    @DeleteMapping("/admin/timetable/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEntry(@PathVariable Long id) {
        timetableService.deleteEntry(id);
        return ResponseEntity.ok(ApiResponse.success("Timetable entry removed."));
    }

    @Data
    static class TimetableRequest {
        private Long classGradeId, subjectId, teacherId;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime, endTime;
        private String roomNumber;
    }
}
