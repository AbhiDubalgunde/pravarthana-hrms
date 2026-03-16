package com.pravarthana.hrms.controller;

import com.pravarthana.hrms.dto.response.DepartmentResponse;
import com.pravarthana.hrms.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * GET /api/departments — list all active departments (any authenticated user)
     */
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    /** GET /api/departments/{id} — single department with teams */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    /** POST /api/departments — create department */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN')")
    public ResponseEntity<DepartmentResponse> create(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.create(body.get("name"), body.get("description")));
    }

    /** PUT /api/departments/{id} — update department */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN')")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        Long headEmployeeId = body.get("headEmployeeId") != null
                ? Long.parseLong(String.valueOf(body.get("headEmployeeId")))
                : null;
        Boolean isActive = body.get("isActive") != null
                ? Boolean.parseBoolean(String.valueOf(body.get("isActive")))
                : null;
        return ResponseEntity.ok(departmentService.update(id, name, description, headEmployeeId, isActive));
    }

    /**
     * DELETE /api/departments/{id} — soft-delete (blocks if employees still
     * assigned)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Department deactivated successfully"));
    }

    /** GET /api/departments/{id}/employees — employees in this department */
    @GetMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN','TEAM_LEAD')")
    public ResponseEntity<DepartmentResponse> getDeptWithEmployees(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }
}
