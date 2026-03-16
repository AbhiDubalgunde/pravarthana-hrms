package com.pravarthana.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned by POST /api/auth/register — never exposes password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedResponse {
    private Long   id;
    private String email;
    private String role;
    private String message;
}
