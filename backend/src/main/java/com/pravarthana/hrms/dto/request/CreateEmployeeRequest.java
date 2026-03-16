package com.pravarthana.hrms.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    /** ID of the department (FK to departments.id) — required for org structure */
    private Long departmentId;
    /** ID of the team within the department (optional) */
    private Long teamId;
    private String designation;
    private LocalDate joiningDate;
    private Long managerId;
    private Long userId;
    private String status; // "ACTIVE" or "INACTIVE" (default ACTIVE)
}
