package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.companyId = :companyId"
         + " AND (:action IS NULL OR a.action LIKE %:action%)"
         + " AND (:email  IS NULL OR a.userId IS NOT NULL)"
         + " AND (:from   IS NULL OR a.createdAt >= :from)"
         + " AND (:to     IS NULL OR a.createdAt <= :to)"
         + " ORDER BY a.createdAt DESC")
    Page<AuditLog> filter(
        @Param("companyId") Long companyId,
        @Param("action")    String action,
        @Param("email")     String email,
        @Param("from")      LocalDateTime from,
        @Param("to")        LocalDateTime to,
        Pageable pageable);
}
