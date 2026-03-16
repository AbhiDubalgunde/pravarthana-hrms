package com.pravarthana.hrms.controller;

import com.pravarthana.hrms.dto.request.CreateEmployeeRequest;
import com.pravarthana.hrms.dto.request.UpdateEmployeeRequest;
import com.pravarthana.hrms.dto.response.EmployeeResponse;
import com.pravarthana.hrms.dto.response.OrgNodeResponse;
import com.pravarthana.hrms.security.JwtTokenProvider;
import com.pravarthana.hrms.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final JwtTokenProvider jwtTokenProvider;

    // GET /api/employees?page=0&size=20&search=&departmentId=&status=
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN','TEAM_LEAD')")
    public ResponseEntity<Page<EmployeeResponse>> getAll(
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "departmentId", defaultValue = "") String departmentId,
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(employeeService.findAll(search, departmentId, status, page, size));
    }

    // GET /api/employees/me — logged-in user's own Employee profile
    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse> getMe(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(employeeService.findByUserId(userId));
    }

    // GET /api/employees/my-team — employees whose managerId == current user's
    // Employee.id
    @GetMapping("/my-team")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN','TEAM_LEAD')")
    public ResponseEntity<List<EmployeeResponse>> getMyTeam(HttpServletRequest request) {
        Long userId = extractUserId(request);
        EmployeeResponse me = employeeService.findByUserId(userId);
        return ResponseEntity.ok(employeeService.findByManagerId(me.getId()));
    }

    // GET /api/employees/org-structure — full company org tree
    @GetMapping("/org-structure")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN','TEAM_LEAD')")
    public ResponseEntity<List<OrgNodeResponse>> getOrgStructure() {
        return ResponseEntity.ok(employeeService.getOrgTree());
    }

    // GET /api/employees/team/{managerId} — direct reports of a manager
    @GetMapping("/team/{managerId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN','TEAM_LEAD')")
    public ResponseEntity<List<EmployeeResponse>> getTeam(@PathVariable Long managerId) {
        return ResponseEntity.ok(employeeService.findByManagerId(managerId));
    }

    // GET /api/employees/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN','TEAM_LEAD')")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    // POST /api/employees
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN')")
    public ResponseEntity<EmployeeResponse> create(@RequestBody CreateEmployeeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(req));
    }

    // PUT /api/employees/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN')")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id,
            @RequestBody UpdateEmployeeRequest req) {
        return ResponseEntity.ok(employeeService.update(id, req));
    }

    // DELETE /api/employees/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Employee deleted successfully"));
    }

    // ── Helper ────────────────────────────────────────────────────
    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing token");
    }
}
