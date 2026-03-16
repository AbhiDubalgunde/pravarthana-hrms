package com.pravarthana.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity for the "companies" table.
 * Represents a SaaS tenant (organisation).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String domain;

    /** Subscription plan: FREE | BASIC | PRO */
    @Column(name = "subscription_plan", nullable = false)
    private String subscriptionPlan = "FREE";

    /** Subscription status: ACTIVE | SUSPENDED | EXPIRED */
    @Column(name = "subscription_status", nullable = false)
    private String subscriptionStatus = "ACTIVE";

    /**
     * Maximum allowed users / employees.
     * -1 means unlimited (PRO plan).
     */
    @Column(name = "max_users", nullable = false)
    private Integer maxUsers = 10;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
