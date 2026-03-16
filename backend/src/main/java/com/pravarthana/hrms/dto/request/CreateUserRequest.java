package com.pravarthana.hrms.dto.request;

import lombok.Data;

/**
 * Request body for POST /api/auth/register
 */
@Data
public class CreateUserRequest {
    private String email;
    private String password;
    /** Role name string: SUPER_ADMIN | HR_ADMIN | TEAM_LEAD | EMPLOYEE */
    private String role;
}
