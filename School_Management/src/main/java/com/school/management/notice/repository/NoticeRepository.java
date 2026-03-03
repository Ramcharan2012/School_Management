package com.school.management.notice.repository;

import com.school.management.common.enums.NoticeTarget;
import com.school.management.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT n FROM Notice n WHERE n.isPublished = true AND " +
            "(n.expiresAt IS NULL OR n.expiresAt > :now) AND " +
            "(n.targetAudience = :target OR n.targetAudience = 'ALL') " +
            "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<Notice> findActiveNoticesForTarget(NoticeTarget target, LocalDateTime now, Pageable pageable);

    Page<Notice> findByIsPublishedTrueOrderByIsPinnedDescPublishedAtDesc(Pageable pageable);
}
