package com.pravarthana.hrms.service;

import com.pravarthana.hrms.dto.request.CreateEmployeeRequest;
import com.pravarthana.hrms.dto.request.UpdateEmployeeRequest;
import com.pravarthana.hrms.dto.response.EmployeeResponse;
import com.pravarthana.hrms.dto.response.OrgNodeResponse;
import com.pravarthana.hrms.entity.Department;
import com.pravarthana.hrms.entity.Employee;
import com.pravarthana.hrms.entity.Employee.EmployeeStatus;
import com.pravarthana.hrms.entity.Team;
import com.pravarthana.hrms.repository.DepartmentRepository;
import com.pravarthana.hrms.repository.EmployeeRepository;
import com.pravarthana.hrms.repository.TeamRepository;
import com.pravarthana.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final SubscriptionService subscriptionService;

    // ── List / Search ─────────────────────────────────────────────
    public Page<EmployeeResponse> findAll(String search, String departmentParam, String status,
            int page, int size) {
        Long companyId = TenantContext.getCompanyId();

        EmployeeStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = EmployeeStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Parse departmentParam — can be a numeric ID (Long) or null/blank
        Long deptId = null;
        if (departmentParam != null && !departmentParam.isBlank()) {
            try {
                deptId = Long.parseLong(departmentParam);
            } catch (NumberFormatException ignored) {
            }
        }

        // searchPattern is ALWAYS non-null: '%' = match all, '%term%' = filter
        String searchPattern = (search == null || search.isBlank())
                ? "%"
                : "%" + search.toLowerCase() + "%";
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<EmployeeResponse> pages = employeeRepository
                .search(companyId, searchPattern, deptId,
                        statusEnum,
                        pageRequest)
                .map(EmployeeResponse::from);

        // Enrich with department names
        Map<Long, String> deptNames = getDeptNameMap(companyId);
        Map<Long, String> teamNames = getTeamNameMap(companyId);
        pages.forEach(r -> enrichResponse(r, deptNames, teamNames));

        return pages;
    }

    // ── Get By ID (tenant-scoped) ─────────────────────────────────
    public EmployeeResponse findById(Long id) {
        Long companyId = TenantContext.getCompanyId();
        EmployeeResponse r = EmployeeResponse.from(getOrThrow(id, companyId));
        enrichResponse(r, getDeptNameMap(companyId), getTeamNameMap(companyId));
        return r;
    }

    // ── Get by User ID (for "me" endpoint) ────────────────────────
    public EmployeeResponse findByUserId(Long userId) {
        Long companyId = TenantContext.getCompanyId();
        Employee emp = employeeRepository.findByUserIdAndCompanyId(userId, companyId)
                .or(() -> employeeRepository.findByUserId(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Employee profile not found for this user"));
        EmployeeResponse r = EmployeeResponse.from(emp);
        enrichResponse(r, getDeptNameMap(companyId), getTeamNameMap(companyId));
        return r;
    }

    // ── Org Tree (Department → Team → Employee) ───────────────────
    public List<OrgNodeResponse> getOrgTree() {
        Long companyId = TenantContext.getCompanyId();
        List<Department> departments = departmentRepository.findByCompanyIdAndIsActiveTrue(companyId);
        List<Team> teams = teamRepository.findByCompanyId(companyId);
        List<Employee> employees = employeeRepository.findByCompanyId(companyId);
        return OrgNodeResponse.buildDeptTree(departments, teams, employees);
    }

    // ── Get team (direct reports of a manager) ────────────────────
    public List<EmployeeResponse> findByManagerId(Long managerId) {
        Long companyId = TenantContext.getCompanyId();
        Map<Long, String> deptNames = getDeptNameMap(companyId);
        Map<Long, String> teamNames = getTeamNameMap(companyId);
        return employeeRepository.findByManagerIdAndCompanyId(managerId, companyId)
                .stream()
                .map(e -> {
                    EmployeeResponse r = EmployeeResponse.from(e);
                    enrichResponse(r, deptNames, teamNames);
                    return r;
                })
                .toList();
    }

    // ── Create ────────────────────────────────────────────────────
    public EmployeeResponse create(CreateEmployeeRequest req) {
        Long companyId = TenantContext.getCompanyId();
        subscriptionService.enforceEmployeeLimit(companyId);

        Employee emp = new Employee();
        emp.setCompanyId(companyId);
        emp.setFirstName(req.getFirstName());
        emp.setLastName(req.getLastName());
        emp.setPhone(req.getPhone());
        emp.setDepartmentId(req.getDepartmentId());
        emp.setTeamId(req.getTeamId());
        emp.setDesignation(req.getDesignation());
        emp.setJoiningDate(req.getJoiningDate());
        emp.setManagerId(req.getManagerId());
        emp.setUserId(req.getUserId());
        emp.setStatus(parseStatus(req.getStatus(), EmployeeStatus.ACTIVE));

        Employee saved = employeeRepository.save(emp);
        saved.setEmployeeCode(generateCode(saved.getId()));
        saved = employeeRepository.save(saved);

        EmployeeResponse r = EmployeeResponse.from(saved);
        enrichResponse(r, getDeptNameMap(companyId), getTeamNameMap(companyId));
        return r;
    }

    // ── Update ────────────────────────────────────────────────────
    public EmployeeResponse update(Long id, UpdateEmployeeRequest req) {
        Long companyId = TenantContext.getCompanyId();
        Employee emp = getOrThrow(id, companyId);

        if (req.getFirstName() != null)
            emp.setFirstName(req.getFirstName());
        if (req.getLastName() != null)
            emp.setLastName(req.getLastName());
        if (req.getPhone() != null)
            emp.setPhone(req.getPhone());
        if (req.getDepartmentId() != null)
            emp.setDepartmentId(req.getDepartmentId());
        if (req.getTeamId() != null)
            emp.setTeamId(req.getTeamId());
        if (req.getDesignation() != null)
            emp.setDesignation(req.getDesignation());
        if (req.getJoiningDate() != null)
            emp.setJoiningDate(req.getJoiningDate());
        if (req.getManagerId() != null)
            emp.setManagerId(req.getManagerId());
        if (req.getStatus() != null)
            emp.setStatus(parseStatus(req.getStatus(), emp.getStatus()));

        EmployeeResponse r = EmployeeResponse.from(employeeRepository.save(emp));
        enrichResponse(r, getDeptNameMap(companyId), getTeamNameMap(companyId));
        return r;
    }

    // ── Delete ────────────────────────────────────────────────────
    public void delete(Long id) {
        Long companyId = TenantContext.getCompanyId();
        getOrThrow(id, companyId);
        employeeRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private Employee getOrThrow(Long id, Long companyId) {
        return employeeRepository.findByIdAndCompanyId(id, companyId)
                .or(() -> employeeRepository.findById(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Employee not found: " + id));
    }

    private String generateCode(Long id) {
        return String.format("EMP%04d", id);
    }

    private EmployeeStatus parseStatus(String raw, EmployeeStatus fallback) {
        if (raw == null)
            return fallback;
        try {
            return EmployeeStatus.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    /** Build dept id → name map for the given company */
    private Map<Long, String> getDeptNameMap(Long companyId) {
        return departmentRepository.findByCompanyId(companyId)
                .stream().collect(Collectors.toMap(Department::getId, Department::getName));
    }

    /** Build team id → name map for the given company */
    private Map<Long, String> getTeamNameMap(Long companyId) {
        return teamRepository.findByCompanyId(companyId)
                .stream().collect(Collectors.toMap(Team::getId, Team::getName));
    }

    /** Enrich response with resolved department + team names */
    private void enrichResponse(EmployeeResponse r, Map<Long, String> deptNames, Map<Long, String> teamNames) {
        if (r.getDepartmentId() != null) {
            String name = deptNames.get(r.getDepartmentId());
            r.setDepartmentName(name);
            r.setDepartment(name); // backward compat alias
        }
        if (r.getTeamId() != null) {
            r.setTeamName(teamNames.get(r.getTeamId()));
        }
    }
}
