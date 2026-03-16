package com.pravarthana.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity for the "subscriptions" table.
 * Tracks billing history per company.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /** Plan code: FREE | BASIC | PRO */
    @Column(nullable = false)
    private String plan;

    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    /** MONTHLY | ANNUAL */
    @Column(name = "billing_cycle")
    private String billingCycle = "MONTHLY";

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** ACTIVE | CANCELLED | EXPIRED */
    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
