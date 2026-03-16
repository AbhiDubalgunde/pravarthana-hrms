package com.pravarthana.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK to companies.id — SaaS multi-tenancy. Nullable at JPA level; DB enforces
     * DEFAULT 1.
     */
    @Column(name = "company_id")
    private Long companyId = 1L;

    @Column(name = "employee_code", unique = true)
    private String employeeCode; // e.g. EMP001

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phone;

    /** FK to departments.id — DO NOT store department name here */
    @Column(name = "department_id")
    private Long departmentId;

    /** FK to teams.id — nullable */
    @Column(name = "team_id")
    private Long teamId;

    private String designation;

    @Column(name = "date_of_joining")
    private LocalDate joiningDate;

    @Column(name = "reporting_manager_id")
    private Long managerId; // references another Employee.id

    @Column(name = "user_id")
    private Long userId; // references users.id (nullable – not all employees need a login)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (companyId == null)
            companyId = 1L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE
    }
}
