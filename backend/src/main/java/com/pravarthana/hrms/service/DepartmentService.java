package com.pravarthana.hrms.service;

import com.pravarthana.hrms.dto.response.DepartmentResponse;
import com.pravarthana.hrms.entity.Department;
import com.pravarthana.hrms.repository.DepartmentRepository;
import com.pravarthana.hrms.repository.EmployeeRepository;
import com.pravarthana.hrms.repository.TeamRepository;
import com.pravarthana.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    // ── List all active departments ───────────────────────────────
    public List<DepartmentResponse> findAll() {
        Long companyId = TenantContext.getCompanyId();
        return departmentRepository.findByCompanyIdAndIsActiveTrue(companyId)
                .stream()
                .map(d -> enrichWithStats(DepartmentResponse.from(d), companyId))
                .collect(Collectors.toList());
    }

    // ── Get by ID ─────────────────────────────────────────────────
    public DepartmentResponse findById(Long id) {
        Long companyId = TenantContext.getCompanyId();
        Department d = getOrThrow(id, companyId);
        DepartmentResponse r = DepartmentResponse.from(d);
        enrichWithStats(r, companyId);
        r.setTeams(
                teamRepository.findByDepartmentIdAndCompanyId(id, companyId)
                        .stream()
                        .map(DepartmentResponse.TeamSummary::from)
                        .collect(Collectors.toList()));
        return r;
    }

    // ── Create ────────────────────────────────────────────────────
    public DepartmentResponse create(String name, String description) {
        Long companyId = TenantContext.getCompanyId();
        if (departmentRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Department '" + name + "' already exists.");
        }
        Department d = new Department();
        d.setCompanyId(companyId);
        d.setName(name);
        d.setDescription(description);
        return DepartmentResponse.from(departmentRepository.save(d));
    }

    // ── Update ────────────────────────────────────────────────────
    public DepartmentResponse update(Long id, String name, String description, Long headEmployeeId, Boolean isActive) {
        Long companyId = TenantContext.getCompanyId();
        Department d = getOrThrow(id, companyId);
        if (name != null)
            d.setName(name);
        if (description != null)
            d.setDescription(description);
        if (headEmployeeId != null)
            d.setHeadEmployeeId(headEmployeeId);
        if (isActive != null)
            d.setIsActive(isActive);
        return DepartmentResponse.from(departmentRepository.save(d));
    }

    // ── Delete (soft-delete via is_active) ────────────────────────
    public void delete(Long id) {
        Long companyId = TenantContext.getCompanyId();
        Department d = getOrThrow(id, companyId);
        long empCount = employeeRepository.findByDepartmentIdAndCompanyId(id, companyId).size();
        if (empCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete department with " + empCount + " active employee(s). Reassign them first.");
        }
        d.setIsActive(false);
        departmentRepository.save(d);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private Department getOrThrow(Long id, Long companyId) {
        return departmentRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Department not found: " + id));
    }

    private DepartmentResponse enrichWithStats(DepartmentResponse r, Long companyId) {
        int count = employeeRepository.findByDepartmentIdAndCompanyId(r.getId(), companyId).size();
        r.setEmployeeCount(count);
        return r;
    }
}
