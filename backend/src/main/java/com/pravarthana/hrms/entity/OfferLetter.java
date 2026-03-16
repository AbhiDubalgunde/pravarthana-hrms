package com.pravarthana.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Matches the ACTUAL offer_letters DB schema:
 * id, employee_id, letter_number (NOT NULL), candidate_name (NOT NULL),
 * candidate_email (NOT NULL), designation (NOT NULL), department,
 * salary (NOT NULL), joining_date (NOT NULL), location,
 * template_content, pdf_url, status, generated_by, generated_at,
 * sent_at, created_at, updated_at
 *
 * Removed columns NOT in DB: company_id, annual_ctc, reporting_manager,
 * working_hours, probation_months, pdf_path, created_by, letter_number set
 * pre-persist.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "offer_letters")
public class OfferLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    /** NOT NULL in DB — must be set BEFORE save */
    @Column(name = "letter_number", nullable = false, unique = true)
    private String letterNumber;

    @Column(name = "candidate_name", nullable = false)
    private String candidateName;

    /** NOT NULL in DB */
    @Column(name = "candidate_email", nullable = false)
    private String candidateEmail;

    /** NOT NULL in DB (maps to 'position/role' in the request) */
    @Column(nullable = false)
    private String designation;

    private String department;

    /** NOT NULL in DB — the entity renamed this salary (not annual_ctc) */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salary;

    /** NOT NULL in DB */
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    private String location;

    @Column(name = "template_content", columnDefinition = "text")
    private String templateContent;

    /** Path/URL where generated PDF is stored */
    @Column(name = "pdf_url")
    private String pdfUrl;

    /** DB default: 'DRAFT' — stored as varchar */
    @Column(nullable = true)
    private String status = "DRAFT";

    @Column(name = "generated_by")
    private Long generatedBy;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        generatedAt = now;
        if (status == null)
            status = "DRAFT";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
