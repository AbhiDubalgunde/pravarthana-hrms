package com.pravarthana.hrms.dto.response;

import com.pravarthana.hrms.entity.Employee;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;

    /** FK reference — numeric ID of the department */
    private Long departmentId;
    /** Resolved department name (set by service when available) */
    private String departmentName;
    /** Legacy alias for backward compat with existing frontend code */
    private String department;

    /** FK reference — numeric ID of the team */
    private Long teamId;
    /** Resolved team name (set by service when available) */
    private String teamName;

    private String designation;
    private LocalDate joiningDate;
    private Long managerId;
    private Long userId;
    private String status;
    private LocalDateTime createdAt;

    public static EmployeeResponse from(Employee e) {
        EmployeeResponse r = new EmployeeResponse();
        r.id = e.getId();
        r.employeeCode = e.getEmployeeCode();
        r.firstName = e.getFirstName();
        r.lastName = e.getLastName();
        r.fullName = e.getFirstName() + (e.getLastName() != null ? " " + e.getLastName() : "");
        r.phone = e.getPhone();
        r.departmentId = e.getDepartmentId();
        r.teamId = e.getTeamId();
        r.designation = e.getDesignation();
        r.joiningDate = e.getJoiningDate();
        r.managerId = e.getManagerId();
        r.userId = e.getUserId();
        r.status = e.getStatus() != null ? e.getStatus().name() : "ACTIVE";
        r.createdAt = e.getCreatedAt();
        // department and departmentName will be enriched by the service when needed
        return r;
    }
}
