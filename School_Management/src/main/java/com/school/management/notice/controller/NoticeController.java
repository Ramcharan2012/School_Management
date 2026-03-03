package com.school.management.notice.controller;

import com.school.management.common.enums.NoticeTarget;
import com.school.management.common.response.ApiResponse;
import com.school.management.common.response.PageResponse;
import com.school.management.notice.entity.Notice;
import com.school.management.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notices", description = "School notice board")
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping("/admin/notices")
    @Operation(summary = "Create a notice (Admin/Teacher)")
    public ResponseEntity<ApiResponse<Notice>> createNotice(@RequestBody CreateNoticeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Notice created.",
                noticeService.createNotice(req.getTitle(), req.getContent(), req.getTargetAudience(),
                        req.getIsPinned(), req.getExpiresAt(), req.getAttachmentUrl(), req.getPostedByUserId())));
    }

    @GetMapping("/notices")
    @Operation(summary = "Get all active notices for a role (ALL, STUDENTS, TEACHERS, etc.)")
    public ResponseEntity<ApiResponse<PageResponse<Notice>>> getNotices(
            @RequestParam(defaultValue = "ALL") NoticeTarget target,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.success(
                noticeService.getActiveNoticesForRole(target, PageRequest.of(page, 10))));
    }

    @GetMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<Notice>> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getById(id)));
    }

    @PatchMapping("/admin/notices/{id}/pin")
    @Operation(summary = "Toggle pin on a notice")
    public ResponseEntity<ApiResponse<Notice>> togglePin(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.togglePin(id)));
    }

    @DeleteMapping("/admin/notices/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success("Notice removed."));
    }

    @Data
    static class CreateNoticeRequest {
        private String title, content, attachmentUrl;
        private NoticeTarget targetAudience = NoticeTarget.ALL;
        private Boolean isPinned = false;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime expiresAt;
        private Long postedByUserId;
    }
}
