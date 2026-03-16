package com.pravarthana.hrms.controller;

import com.pravarthana.hrms.dto.request.LeaveRequest;
import com.pravarthana.hrms.dto.response.LeaveResponse;
import com.pravarthana.hrms.repository.EmployeeRepository;
import com.pravarthana.hrms.security.JwtTokenProvider;
import com.pravarthana.hrms.service.LeaveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService       leaveService;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider   jwtTokenProvider;

    /** POST /api/leaves — employee applies for leave */
    @PostMapping
    public ResponseEntity<LeaveResponse> apply(
            @Valid @RequestBody LeaveRequest req,
            HttpServletRequest httpReq) {
        Long empId = resolveEmployeeId(httpReq);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.apply(empId, req));
    }

    /** GET /api/leaves/my — employee's own leaves */
    @GetMapping("/my")
    public ResponseEntity<List<LeaveResponse>> myLeaves(HttpServletRequest httpReq) {
        Long empId = resolveEmployeeId(httpReq);
        return ResponseEntity.ok(leaveService.getMyLeaves(empId));
    }

    /** GET /api/leaves/pending — HR/Admin only */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN')")
    public ResponseEntity<Page<LeaveResponse>> pending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(leaveService.getPending(page, size));
    }

    /** GET /api/leaves — all company leaves (HR/Admin) */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN')")
    public ResponseEntity<Page<LeaveResponse>> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(leaveService.getAll(page, size));
    }

    /** PUT /api/leaves/{id}/approve */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN')")
    public ResponseEntity<LeaveResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.approve(id));
    }

    /** PUT /api/leaves/{id}/reject  body: { "reason": "..." } */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN')")
    public ResponseEntity<LeaveResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return ResponseEntity.ok(leaveService.reject(id, reason));
    }

    // ── Helper: JWT → employeeId ──────────────────────────────────
    private Long resolveEmployeeId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            Long userId = jwtTokenProvider.getUserIdFromToken(header.substring(7));
            return employeeRepository.findByUserId(userId)
                    .map(e -> e.getId())
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND,
                            "Employee profile not found for this user."));
        }
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing token");
    }
}
