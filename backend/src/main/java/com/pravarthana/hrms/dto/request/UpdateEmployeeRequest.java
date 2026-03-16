package com.pravarthana.hrms.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateEmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    /** FK to departments.id */
    private Long departmentId;
    /** FK to teams.id (optional) */
    private Long teamId;
    private String designation;
    private LocalDate joiningDate;
    private Long managerId;
    private String status; // "ACTIVE" or "INACTIVE"
}
