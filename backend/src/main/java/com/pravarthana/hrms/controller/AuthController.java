package com.pravarthana.hrms.controller;

import com.pravarthana.hrms.dto.request.CreateUserRequest;
import com.pravarthana.hrms.dto.request.LoginRequest;
import com.pravarthana.hrms.dto.response.AuthResponse;
import com.pravarthana.hrms.dto.response.UserCreatedResponse;
import com.pravarthana.hrms.service.AuthService;
import com.pravarthana.hrms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AuthController — thin controller.
 * No repository access. No password logic. No JWT generation.
 * All business logic lives in AuthService and UserService.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserService userService;

    // ─── Public ────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status",    "UP",
            "timestamp", LocalDateTime.now().toString(),
            "message",   "Pravarthana HRMS Backend is running!"
        ));
    }

    /**
     * Login — delegates entirely to AuthService.
     * Returns {token, user{id, email, role, fullName, employee}}.
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    /**
     * Register — delegates entirely to UserService.
     * Body: { "email": "...", "password": "...", "role": "EMPLOYEE" }
     * Returns 201 Created with UserCreatedResponse (no password).
     */
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequest request) {
        try {
            UserCreatedResponse response = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed. Please try again."));
        }
    }

    /**
     * Logout — JWT is stateless; client must discard the token + cookie.
     * For full revocation, implement a token denylist (Redis).
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of(
            "message",   "Logged out successfully. Please clear your token.",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ─── Protected RBAC endpoints ──────────────────────────────────────

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> adminDashboard() {
        return ResponseEntity.ok(Map.of("message", "Super Admin dashboard access granted."));
    }

    @GetMapping("/hr/employees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<Map<String, String>> hrEmployees() {
        return ResponseEntity.ok(Map.of("message", "HR employees access granted."));
    }

    @GetMapping("/team/members")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'TEAM_LEAD')")
    public ResponseEntity<Map<String, String>> teamMembers() {
        return ResponseEntity.ok(Map.of("message", "Team members access granted."));
    }

    @GetMapping("/employee/profile")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'TEAM_LEAD', 'EMPLOYEE')")
    public ResponseEntity<Map<String, String>> employeeProfile() {
        return ResponseEntity.ok(Map.of("message", "Employee profile access granted."));
    }
}
