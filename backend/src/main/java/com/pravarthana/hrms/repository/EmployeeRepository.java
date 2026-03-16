package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.Employee;
import com.pravarthana.hrms.entity.Employee.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

        // ── Single-tenant legacy (kept for compatibility) ───────────────────
        Optional<Employee> findByUserId(Long userId);

        List<Employee> findByManagerId(Long managerId);

        // ── Multi-tenant: all queries scoped by companyId ────────────────────

        /** Count employees for a given company — used by SubscriptionService */
        long countByCompanyId(Long companyId);

        /** All employees in a company — used for org tree */
        List<Employee> findByCompanyId(Long companyId);

        /** Retrieve employee by userId, scoped to company */
        Optional<Employee> findByUserIdAndCompanyId(Long userId, Long companyId);

        /** Retrieve employee by id, scoped to company */
        Optional<Employee> findByIdAndCompanyId(Long id, Long companyId);

        /** Team members of a given manager, scoped to company */
        List<Employee> findByManagerIdAndCompanyId(Long managerId, Long companyId);

        /** Employees in a specific department, scoped to company */
        List<Employee> findByDepartmentIdAndCompanyId(Long departmentId, Long companyId);

        /** Employees in a specific team, scoped to company */
        List<Employee> findByTeamIdAndCompanyId(Long teamId, Long companyId);

        /**
         * Full-text search with optional filters.
         *
         * CRITICAL: :searchPattern must NEVER be null — pass '%' (match-all) or
         * '%term%'.
         * This avoids the Hibernate/PostgreSQL "lower(bytea) does not exist" error
         * which occurs when null is bound to a LOWER() call without type info.
         *
         * companyId may be null — when null, the company filter is skipped.
         */
        @Query("SELECT e FROM Employee e WHERE " +
                        "(:companyId IS NULL OR e.companyId = :companyId) " +
                        "AND (LOWER(e.firstName)                          LIKE :searchPattern " +
                        " OR  LOWER(e.lastName)                           LIKE :searchPattern " +
                        " OR  LOWER(COALESCE(e.employeeCode, ''))         LIKE :searchPattern) " +
                        "AND (:departmentId IS NULL OR e.departmentId = :departmentId) " +
                        "AND (:status IS NULL OR e.status = :status) " +
                        "ORDER BY e.createdAt DESC")
        Page<Employee> search(
                        @Param("companyId") Long companyId,
                        @Param("searchPattern") String searchPattern,
                        @Param("departmentId") Long departmentId,
                        @Param("status") EmployeeStatus status,
                        Pageable pageable);

        /** Latest employee codes — used for auto-generating EMP00N */
        @Query("SELECT e.employeeCode FROM Employee e ORDER BY e.id DESC")
        List<String> findAllEmployeeCodes(Pageable pageable);
}
