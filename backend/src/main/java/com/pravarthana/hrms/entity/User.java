package com.pravarthana.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity mapping to the "users" table in Supabase/PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to companies.id — SaaS multi-tenancy. Nullable at JPA level; DB enforces DEFAULT 1. */
    @Column(name = "company_id")
    private Long companyId = 1L;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** FK to roles.id — stored as Long to match PostgreSQL SERIAL/BIGINT */
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    // ── Security hardening: account lockout ──────────────────────────
    /** Number of consecutive failed login attempts. Reset on success. */
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    /** When non-null, account is locked until this timestamp. */
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (failedLoginAttempts == null) failedLoginAttempts = 0;
        if (companyId == null) companyId = 1L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
