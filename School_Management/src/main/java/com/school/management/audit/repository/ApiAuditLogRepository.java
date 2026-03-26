package com.school.management.audit.repository;

import com.school.management.audit.entity.ApiAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiAuditLogRepository extends JpaRepository<ApiAuditLog, Long> {
}
