package com.pravarthana.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity for the "designations" table.
 * Normalised from VARCHAR column in employees.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "designations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "title"}))
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String title;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
