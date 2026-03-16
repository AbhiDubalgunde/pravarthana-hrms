package com.pravarthana.hrms.service;

import com.pravarthana.hrms.constants.RoleConstants;
import com.pravarthana.hrms.dto.request.CreateUserRequest;
import com.pravarthana.hrms.dto.response.UserCreatedResponse;
import com.pravarthana.hrms.entity.User;
import com.pravarthana.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * UserService — all user creation logic lives here.
 *
 * Design rules:
 * - Passwords are ONLY hashed inside this service.
 * - Controllers never touch UserRepository directly.
 * - Never store or return plaintext passwords.
 */
@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * Creates a new user account.
     *
     * @param request  email + plaintext password + role name
     * @return         UserCreatedResponse (no password returned)
     * @throws IllegalArgumentException if email is already in use or role is invalid
     */
    public UserCreatedResponse createUser(CreateUserRequest request) {
        // 1. Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        // 2. Resolve role ID from role name
        Long roleId = resolveRoleId(request.getRole());

        // 3. Hash password (only service layer does this)
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. Build and persist user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(encodedPassword);
        user.setRoleId(roleId);
        user.setIsActive(true);

        User saved = userRepository.save(user);

        return new UserCreatedResponse(
            saved.getId(),
            saved.getEmail(),
            RoleConstants.fromRoleId(saved.getRoleId()),
            "User created successfully"
        );
    }

    /**
     * Idempotent user creation — skips silently if email already exists.
     * Used by DevDataSeeder so re-seeding is safe.
     */
    public void createUserIfNotExists(String email, String rawPassword, Long roleId) {
        if (userRepository.existsByEmail(email)) return;

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRoleId(roleId);
        user.setIsActive(true);
        userRepository.save(user);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    /**
     * Maps role name string to its DB ID.
     * Throws if the role string is unrecognised.
     */
    private Long resolveRoleId(String role) {
        if (role == null) throw new IllegalArgumentException("Role must not be null");
        switch (role.toUpperCase()) {
            case RoleConstants.SUPER_ADMIN: return RoleConstants.SUPER_ADMIN_ID;
            case RoleConstants.HR_ADMIN:    return RoleConstants.HR_ADMIN_ID;
            case RoleConstants.TEAM_LEAD:   return RoleConstants.TEAM_LEAD_ID;
            case RoleConstants.EMPLOYEE:    return RoleConstants.EMPLOYEE_ID;
            default:
                throw new IllegalArgumentException("Unknown role: " + role + 
                    ". Valid roles: SUPER_ADMIN, HR_ADMIN, TEAM_LEAD, EMPLOYEE");
        }
    }
}
