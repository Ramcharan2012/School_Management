package com.school.management.notice.service;

import com.school.management.common.enums.NoticeTarget;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.response.PageResponse;
import com.school.management.notice.entity.Notice;
import com.school.management.notice.repository.NoticeRepository;
import com.school.management.user.entity.User;
import com.school.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepo;
    private final UserRepository userRepo;

    @Transactional
    public Notice createNotice(String title, String content, NoticeTarget target,
            Boolean isPinned, LocalDateTime expiresAt,
            String attachmentUrl, Long postedByUserId) {
        User user = userRepo.findById(postedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", postedByUserId));
        return noticeRepo.save(Notice.builder()
                .title(title).content(content).targetAudience(target)
                .isPinned(isPinned != null && isPinned)
                .isPublished(true).publishedAt(LocalDateTime.now())
                .expiresAt(expiresAt).attachmentUrl(attachmentUrl).postedBy(user)
                .build());
    }

    public PageResponse<Notice> getActiveNoticesForRole(NoticeTarget target, Pageable pageable) {
        return PageResponse.of(noticeRepo.findActiveNoticesForTarget(target, LocalDateTime.now(), pageable));
    }

    public PageResponse<Notice> getAllNotices(Pageable pageable) {
        return PageResponse.of(noticeRepo.findByIsPublishedTrueOrderByIsPinnedDescPublishedAtDesc(pageable));
    }

    public Notice getById(Long id) {
        return noticeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notice", id));
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = getById(id);
        notice.setIsPublished(false);
        noticeRepo.save(notice);
    }

    @Transactional
    public Notice togglePin(Long id) {
        Notice notice = getById(id);
        notice.setIsPinned(!notice.getIsPinned());
        return noticeRepo.save(notice);
    }
}
