package com.pravarthana.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity for the "audit_logs" table.
 * Records every significant action for compliance and tracing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "user_id")
    private Long userId;

    /** Action performed, e.g. "EMPLOYEE_CREATED", "LOGIN_SUCCESS" */
    @Column(nullable = false, length = 100)
    private String action;

    /** Entity type, e.g. "Employee", "User" */
    @Column(name = "entity_type", length = 100)
    private String entityType;

    /** PK of the entity that was affected */
    @Column(name = "entity_id")
    private Long entityId;

    /** Unique request trace ID from X-Request-ID header */
    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /** Optional free-form detail string (JSON) */
    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
