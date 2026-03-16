package com.pravarthana.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String role;
        private String fullName;   // New: "FirstName LastName" from Employee table
        private EmployeeInfo employee;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeInfo {
        private Long id;
        private String employeeId;
        private String firstName;
        private String lastName;
    }
}
