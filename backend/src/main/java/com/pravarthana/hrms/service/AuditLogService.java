package com.pravarthana.hrms.service;

import com.pravarthana.hrms.entity.AuditLog;
import com.pravarthana.hrms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AuditLogService — records significant actions asynchronously.
 * Uses @Async so it never slows down the main request path.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Persist an audit log entry.
     * Called asynchronously — does not block the caller.
     */
    @Async
    public void log(Long userId, Long companyId,
                    String action, String entityType, Long entityId,
                    String requestId, String ipAddress) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .companyId(companyId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .requestId(requestId)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Audit log failure must never break the main request
        }
    }

    /**
     * Convenience overload — logs without entity context.
     */
    @Async
    public void log(Long userId, Long companyId, String action, String requestId, String ipAddress) {
        log(userId, companyId, action, null, null, requestId, ipAddress);
    }
}
