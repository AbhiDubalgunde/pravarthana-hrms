package com.pravarthana.hrms.dto.response;

import com.pravarthana.hrms.entity.Department;
import com.pravarthana.hrms.entity.Employee;
import com.pravarthana.hrms.entity.Team;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Org structure is Department → Team → Employee hierarchy.
 * The root nodes are departments; each has a list of teams and
 * a list of employees not assigned to any team.
 */
@Data
public class OrgNodeResponse {

    /* ── Department-level fields ── */
    private String type; // "department" | "team" | "employee"
    private Long id;
    private String name;

    /* ── Employee-level fields ── */
    private String firstName;
    private String lastName;
    private String designation;
    private String departmentName;
    private String employeeCode;
    private Long managerId;

    /* ── Children ── */
    private List<OrgNodeResponse> teams = new ArrayList<>(); // only on department nodes
    private List<OrgNodeResponse> employees = new ArrayList<>(); // on dept (no team) or team nodes

    // ── Factory helpers ──────────────────────────────────────────

    public static OrgNodeResponse fromDepartment(Department d) {
        OrgNodeResponse node = new OrgNodeResponse();
        node.setType("department");
        node.setId(d.getId());
        node.setName(d.getName());
        return node;
    }

    public static OrgNodeResponse fromTeam(Team t) {
        OrgNodeResponse node = new OrgNodeResponse();
        node.setType("team");
        node.setId(t.getId());
        node.setName(t.getName());
        return node;
    }

    public static OrgNodeResponse fromEmployee(Employee e, String deptName) {
        OrgNodeResponse node = new OrgNodeResponse();
        node.setType("employee");
        node.setId(e.getId());
        node.setFirstName(e.getFirstName());
        node.setLastName(e.getLastName() != null ? e.getLastName() : "");
        node.setName((e.getFirstName() + " " + (e.getLastName() != null ? e.getLastName() : "")).trim());
        node.setDesignation(e.getDesignation());
        node.setDepartmentName(deptName);
        node.setEmployeeCode(e.getEmployeeCode());
        node.setManagerId(e.getManagerId());
        return node;
    }

    /**
     * Builds a department-first org forest.
     * Structure: Department → Team → Employees (employees with no team go directly
     * under dept)
     *
     * @param departments all departments for the company
     * @param teams       all teams for the company
     * @param employees   all employees for the company
     */
    public static List<OrgNodeResponse> buildDeptTree(
            List<Department> departments,
            List<Team> teams,
            List<Employee> employees) {

        // Index: deptId → dept name
        Map<Long, String> deptNames = departments.stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));

        // Index: teamId → team node
        Map<Long, OrgNodeResponse> teamNodes = teams.stream()
                .collect(Collectors.toMap(Team::getId, OrgNodeResponse::fromTeam));

        // Index: teamId → list of employees in that team
        Map<Long, List<OrgNodeResponse>> empsByTeam = teams.stream()
                .collect(Collectors.toMap(Team::getId, t -> new ArrayList<>()));

        // Index: deptId → list of teams
        Map<Long, List<OrgNodeResponse>> teamsByDept = departments.stream()
                .collect(Collectors.toMap(Department::getId, d -> new ArrayList<>()));

        // Index: deptId → employees with no team
        Map<Long, List<OrgNodeResponse>> empsByDeptNoTeam = departments.stream()
                .collect(Collectors.toMap(Department::getId, d -> new ArrayList<>()));

        // Populate team → dept index
        for (Team t : teams) {
            if (teamsByDept.containsKey(t.getDepartmentId())) {
                teamsByDept.get(t.getDepartmentId()).add(teamNodes.get(t.getId()));
            }
        }

        // Distribute employees
        for (Employee e : employees) {
            String deptName = e.getDepartmentId() != null ? deptNames.getOrDefault(e.getDepartmentId(), "") : "";
            OrgNodeResponse empNode = fromEmployee(e, deptName);

            if (e.getTeamId() != null && empsByTeam.containsKey(e.getTeamId())) {
                empsByTeam.get(e.getTeamId()).add(empNode);
            } else if (e.getDepartmentId() != null && empsByDeptNoTeam.containsKey(e.getDepartmentId())) {
                empsByDeptNoTeam.get(e.getDepartmentId()).add(empNode);
            }
        }

        // Attach employees to teams
        for (Map.Entry<Long, List<OrgNodeResponse>> entry : empsByTeam.entrySet()) {
            OrgNodeResponse teamNode = teamNodes.get(entry.getKey());
            if (teamNode != null) {
                teamNode.setEmployees(entry.getValue());
            }
        }

        // Build department nodes
        List<OrgNodeResponse> result = new ArrayList<>();
        for (Department d : departments) {
            if (d.getIsActive() == null || !d.getIsActive())
                continue;
            OrgNodeResponse deptNode = fromDepartment(d);
            deptNode.setTeams(teamsByDept.getOrDefault(d.getId(), new ArrayList<>()));
            deptNode.setEmployees(empsByDeptNoTeam.getOrDefault(d.getId(), new ArrayList<>()));
            result.add(deptNode);
        }

        return result;
    }

    /**
     * Legacy manager-hierarchy tree (kept for backward compat with /my-team type
     * views).
     * Employees with null managerId or whose manager is not in the list become
     * roots.
     */
    public static List<OrgNodeResponse> buildManagerTree(List<Employee> employees) {
        Map<Long, OrgNodeResponse> nodeMap = employees.stream()
                .collect(Collectors.toMap(Employee::getId, e -> fromEmployee(e, null)));

        List<OrgNodeResponse> roots = new ArrayList<>();
        for (Employee emp : employees) {
            OrgNodeResponse node = nodeMap.get(emp.getId());
            if (emp.getManagerId() == null || !nodeMap.containsKey(emp.getManagerId())) {
                roots.add(node);
            } else {
                nodeMap.get(emp.getManagerId()).getEmployees().add(node);
            }
        }
        return roots;
    }
}
