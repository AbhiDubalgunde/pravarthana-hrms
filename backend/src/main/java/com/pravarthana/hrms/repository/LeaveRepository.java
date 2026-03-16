package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.Leave;
import com.pravarthana.hrms.entity.Leave.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    /** Simple employee-scoped leave list (no company_id on leaves table) */
    List<Leave> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    /** Paginated leaves by status for HR — no company filter */
    Page<Leave> findByStatusOrderByCreatedAtDesc(LeaveStatus status, Pageable pageable);

    /** All leaves — no company filter */
    Page<Leave> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Count for dashboard — no company_id on leaves table */
    long countByStatus(LeaveStatus status);

    // ── Legacy company-scoped methods kept for API compat ───────────────────
    List<Leave> findByCompanyIdAndEmployeeIdOrderByStartDateDesc(Long companyId, Long employeeId);

    Page<Leave> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, LeaveStatus status, Pageable pageable);

    Page<Leave> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);

    long countByCompanyIdAndStatus(Long companyId, LeaveStatus status);
}
