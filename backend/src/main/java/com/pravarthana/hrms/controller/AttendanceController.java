package com.pravarthana.hrms.controller;

import com.pravarthana.hrms.dto.response.AttendanceResponse;
import com.pravarthana.hrms.repository.EmployeeRepository;
import com.pravarthana.hrms.security.JwtTokenProvider;
import com.pravarthana.hrms.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // POST /api/attendance/punch-in  (auto-detects employee from JWT)
    @PostMapping("/punch-in")
    public ResponseEntity<AttendanceResponse> punchIn(HttpServletRequest request) {
        Long empId = resolveEmployeeId(request);
        return ResponseEntity.ok(attendanceService.punchIn(empId));
    }

    // POST /api/attendance/punch-out
    @PostMapping("/punch-out")
    public ResponseEntity<AttendanceResponse> punchOut(HttpServletRequest request) {
        Long empId = resolveEmployeeId(request);
        return ResponseEntity.ok(attendanceService.punchOut(empId));
    }

    // GET /api/attendance/today/{employeeId}
    @GetMapping("/today/{employeeId}")
    public ResponseEntity<AttendanceResponse> getToday(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getToday(employeeId));
    }

    // GET /api/attendance/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN','TEAM_LEAD')")
    public ResponseEntity<List<AttendanceResponse>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getByEmployee(employeeId));
    }

    // GET /api/attendance/monthly/{employeeId}?month=YYYY-MM
    // Role rules:
    //   EMPLOYEE   → only their own record
    //   TEAM_LEAD  → only their direct subordinates
    //   HR_ADMIN / SUPER_ADMIN → unrestricted
    @GetMapping("/monthly/{employeeId}")
    public ResponseEntity<List<AttendanceResponse>> getMonthly(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "") String month,
            HttpServletRequest request) {
        if (month.isBlank()) month = YearMonth.now().toString();

        String token = extractToken(request);
        String role  = (token != null) ? jwtTokenProvider.getRoleFromToken(token) : "EMPLOYEE";
        Long   userId = (token != null) ? jwtTokenProvider.getUserIdFromToken(token) : null;

        if ("SUPER_ADMIN".equals(role) || "HR_ADMIN".equals(role)) {
            // unrestricted
        } else if ("TEAM_LEAD".equals(role)) {
            // Must be a direct subordinate of the requester
            Long managerId = employeeRepository.findByUserId(userId)
                    .map(e -> e.getId()).orElse(null);
            Long empManagerId = employeeRepository.findById(employeeId)
                    .map(e -> e.getManagerId()).orElse(null);
            if (managerId == null || !managerId.equals(empManagerId)) {
                return ResponseEntity.status(403).build();
            }
        } else {
            // EMPLOYEE — only own data
            Long myEmpId = employeeRepository.findByUserId(userId)
                    .map(e -> e.getId()).orElse(null);
            if (!employeeId.equals(myEmpId)) {
                return ResponseEntity.status(403).build();
            }
        }

        return ResponseEntity.ok(attendanceService.getMonthly(employeeId, month));
    }

    // ── Helpers ───────────────────────────────────────────────────
    private Long resolveEmployeeId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing token");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return employeeRepository.findByUserId(userId)
                .map(e -> e.getId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Employee profile not found for this user. Please contact HR."));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
