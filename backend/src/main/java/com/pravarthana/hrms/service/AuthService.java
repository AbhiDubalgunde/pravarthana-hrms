package com.pravarthana.hrms.service;

import com.pravarthana.hrms.constants.RoleConstants;
import com.pravarthana.hrms.dto.request.LoginRequest;
import com.pravarthana.hrms.dto.response.AuthResponse;
import com.pravarthana.hrms.entity.User;
import com.pravarthana.hrms.repository.UserRepository;
import com.pravarthana.hrms.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

/**
 * AuthService — all login business logic lives here.
 *
 * Design rules:
 * - AuthController delegates entirely to this service.
 * - Password matching happens only here.
 * - JWT generation happens only here.
 * - Account lockout after 5 failed attempts (locked for 15 minutes).
 */
@Service
public class AuthService {

    private static final int    MAX_FAILED_ATTEMPTS = 5;
    private static final int    LOCK_DURATION_MINUTES = 15;

    @Autowired private UserRepository    userRepository;
    @Autowired private PasswordEncoder   passwordEncoder;
    @Autowired private JwtTokenProvider  jwtTokenProvider;

    /**
     * Authenticates a user and returns a JWT response.
     *
     * @throws ResponseStatusException 401 on bad credentials, 403 on locked/inactive account
     */
    public AuthResponse login(LoginRequest request) {

        // 1. Look up user (return same error as bad password to prevent email enumeration)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // 2. Check account lock
        if (isLocked(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Account is temporarily locked due to too many failed attempts. " +
                "Please try again after " + LOCK_DURATION_MINUTES + " minutes.");
        }

        // 3. Check active status
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Account is inactive. Contact your administrator.");
        }

        // 4. Verify password — BCrypt.matches(plain, hash)
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            incrementFailedAttempts(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // 5. Successful login — reset lockout state and update last-login
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 6. Build JWT with email + role + companyId embedded
        String roleName  = RoleConstants.fromRoleId(user.getRoleId());
        Long   companyId = user.getCompanyId() != null ? user.getCompanyId() : 1L;
        String token     = jwtTokenProvider.generateToken(user.getEmail(), roleName, user.getId(), companyId);

        // 7. Build response — password is never included
        AuthResponse.EmployeeInfo emp = new AuthResponse.EmployeeInfo(
            user.getId(),
            "EMP" + String.format("%03d", user.getId()),
            resolveFirstName(user.getEmail()),
            ""
        );

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
            user.getId(),
            user.getEmail(),
            roleName,
            emp.getFirstName(),
            emp
        );

        return new AuthResponse(token, userInfo);
    }

    // ── Private helpers ───────────────────────────────────────────────

    /** Returns true if the account is currently locked. */
    private boolean isLocked(User user) {
        if (user.getAccountLockedUntil() == null) return false;
        if (LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            // Lock has expired — clear it
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
            return false;
        }
        return true;
    }

    /** Increments the failed attempt counter; locks the account after MAX_FAILED_ATTEMPTS. */
    private void incrementFailedAttempts(User user) {
        int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        userRepository.save(user);
    }

    /** Derives a display first name from the email local part. */
    private String resolveFirstName(String email) {
        if (email == null) return "User";
        String local = email.split("@")[0];
        String first = local.contains(".") ? local.split("\\.")[0] : local;
        if (first.isEmpty()) return "User";
        return Character.toUpperCase(first.charAt(0)) + first.substring(1);
    }
}
