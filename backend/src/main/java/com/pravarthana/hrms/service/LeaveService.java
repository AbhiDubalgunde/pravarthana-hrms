package com.pravarthana.hrms.service;

import com.pravarthana.hrms.dto.request.LeaveRequest;
import com.pravarthana.hrms.dto.response.LeaveResponse;
import com.pravarthana.hrms.entity.Attendance;
import com.pravarthana.hrms.entity.Leave;
import com.pravarthana.hrms.entity.Leave.LeaveStatus;
import com.pravarthana.hrms.repository.AttendanceRepository;
import com.pravarthana.hrms.repository.EmployeeRepository;
import com.pravarthana.hrms.repository.LeaveRepository;
import com.pravarthana.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    // ── Apply Leave ───────────────────────────────────────────────
    @Transactional
    public LeaveResponse apply(Long employeeId, LeaveRequest req) {
        Long companyId = TenantContext.getCompanyId();

        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        }

        int days = (int) ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;

        Leave leave = new Leave();
        leave.setCompanyId(companyId);
        leave.setEmployeeId(employeeId);
        leave.setLeaveType(req.getLeaveType() != null ? req.getLeaveType() : "CASUAL");
        leave.setStartDate(req.getStartDate());
        leave.setEndDate(req.getEndDate());
        leave.setDays(days);
        leave.setReason(req.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        return LeaveResponse.from(leaveRepository.save(leave));
    }

    // ── Approve Leave ────────────────────────────────────────────
    @Transactional
    public LeaveResponse approve(Long leaveId) {
        Long companyId = TenantContext.getCompanyId();
        Long reviewerId = TenantContext.getUserId();

        Leave leave = getLeaveForCompany(leaveId, companyId);
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Leave is no longer pending.");
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedBy(reviewerId);
        leave.setReviewedAt(LocalDateTime.now());
        leaveRepository.save(leave);

        // Mark attendance rows as LEAVE for each day
        markAttendanceAsLeave(leave);

        return LeaveResponse.from(leave);
    }

    // ── Reject Leave ─────────────────────────────────────────────
    @Transactional
    public LeaveResponse reject(Long leaveId, String reason) {
        Long companyId = TenantContext.getCompanyId();
        Long reviewerId = TenantContext.getUserId();

        Leave leave = getLeaveForCompany(leaveId, companyId);
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Leave is no longer pending.");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setReviewedBy(reviewerId);
        leave.setReviewedAt(LocalDateTime.now());
        leave.setRejectionReason(reason);

        return LeaveResponse.from(leaveRepository.save(leave));
    }

    // ── My Leaves (employee view) ─────────────────────────────────
    public List<LeaveResponse> getMyLeaves(Long employeeId) {
        Long companyId = TenantContext.getCompanyId();
        return leaveRepository
                .findByCompanyIdAndEmployeeIdOrderByStartDateDesc(companyId, employeeId)
                .stream().map(LeaveResponse::from).collect(Collectors.toList());
    }

    // ── Pending Leaves (HR/Admin view) ───────────────────────────
    public Page<LeaveResponse> getPending(int page, int size) {
        Long companyId = TenantContext.getCompanyId();
        return leaveRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(
                companyId, LeaveStatus.PENDING,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(l -> enrichWithEmployee(LeaveResponse.from(l)));
    }

    // ── All Leaves for company (HR/Admin) ────────────────────────
    public Page<LeaveResponse> getAll(int page, int size) {
        Long companyId = TenantContext.getCompanyId();
        return leaveRepository.findByCompanyIdOrderByCreatedAtDesc(
                companyId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(l -> enrichWithEmployee(LeaveResponse.from(l)));
    }

    // ── Private helpers ──────────────────────────────────────────

    private Leave getLeaveForCompany(Long leaveId, Long companyId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave not found."));
        if (!leave.getCompanyId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        return leave;
    }

    /** Create LEAVE attendance records for each approved leave day */
    private void markAttendanceAsLeave(Leave leave) {
        LocalDate d = leave.getStartDate();
        while (!d.isAfter(leave.getEndDate())) {
            final LocalDate day = d;
            Attendance att = attendanceRepository
                    .findByEmployeeIdAndDate(leave.getEmployeeId(), day)
                    .orElseGet(() -> {
                        Attendance a = new Attendance();
                        a.setEmployeeId(leave.getEmployeeId());
                        a.setDate(day);
                        return a;
                    });
            att.setStatus(Attendance.AttendanceStatus.LEAVE);
            attendanceRepository.save(att);
            d = d.plusDays(1);
        }
    }

    /** Add employee name/dept to leave response for HR view */
    private LeaveResponse enrichWithEmployee(LeaveResponse r) {
        employeeRepository.findById(r.getEmployeeId()).ifPresent(emp -> {
            r.setEmployeeName((emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "")).trim());
            // departmentId is a Long FK — convert to string for the response display field
            r.setDepartment(emp.getDepartmentId() != null ? String.valueOf(emp.getDepartmentId()) : null);
        });
        return r;
    }
}
