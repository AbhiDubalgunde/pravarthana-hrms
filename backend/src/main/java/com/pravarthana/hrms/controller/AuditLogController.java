package com.pravarthana.hrms.controller;

import com.pravarthana.hrms.entity.AuditLog;
import com.pravarthana.hrms.repository.AuditLogRepository;
import com.pravarthana.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    /**
     * GET /api/audit-logs
     * Params: action=, startDate=YYYY-MM-DD, endDate=YYYY-MM-DD, page=, size=
     */
    @GetMapping
    public ResponseEntity<Page<AuditLog>> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {

        Long companyId = TenantContext.getCompanyId();

        LocalDateTime from = startDate != null ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime to   = endDate   != null ? LocalDate.parse(endDate).atTime(23, 59, 59) : null;

        boolean hasFilters = (action != null && !action.isBlank()) || from != null || to != null;

        Page<AuditLog> result = hasFilters
            ? auditLogRepository.filter(
                companyId,
                (action != null && !action.isBlank()) ? action : null,
                email,
                from, to,
                PageRequest.of(page, size))
            : auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(
                companyId,
                PageRequest.of(page, size));

        return ResponseEntity.ok(result);
    }
}
