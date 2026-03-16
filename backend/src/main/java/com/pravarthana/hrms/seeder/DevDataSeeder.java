package com.pravarthana.hrms.seeder;

import com.pravarthana.hrms.constants.RoleConstants;
import com.pravarthana.hrms.repository.UserRepository;
import com.pravarthana.hrms.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * DevDataSeeder — seeds default accounts on first startup in the "dev" profile.
 *
 * Safety rules:
 * ✔ Only active in the "dev" Spring profile
 * ✔ Never deletes existing users
 * ✔ Never overwrites existing users (createUserIfNotExists is idempotent)
 * ✔ Delegates to UserService — never handles passwords directly
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    @Autowired private UserRepository userRepository;
    @Autowired private UserService    userService;

    @Override
    public void run(String... args) {
        long existingCount = userRepository.count();

        if (existingCount > 0) {
            log.info("[DevDataSeeder] {} user(s) found — skipping seed (data already present).", existingCount);
            return;
        }

        log.info("[DevDataSeeder] Empty users table detected — seeding default dev accounts...");

        userService.createUserIfNotExists("admin@pravarthana.com",    "admin@123",    RoleConstants.SUPER_ADMIN_ID);
        userService.createUserIfNotExists("hr@pravarthana.com",       "headhr@123",   RoleConstants.HR_ADMIN_ID);
        userService.createUserIfNotExists("manager@pravarthana.com",  "teamlead@123", RoleConstants.TEAM_LEAD_ID);
        userService.createUserIfNotExists("employee@pravarthana.com", "employee@123", RoleConstants.EMPLOYEE_ID);

        log.info("[DevDataSeeder] ✅ Default accounts seeded:");
        log.info("  admin@pravarthana.com    → SUPER_ADMIN  (admin@123)");
        log.info("  hr@pravarthana.com       → HR_ADMIN     (headhr@123)");
        log.info("  manager@pravarthana.com  → TEAM_LEAD    (teamlead@123)");
        log.info("  employee@pravarthana.com → EMPLOYEE     (employee@123)");
        log.warn("[DevDataSeeder] ⚠ Change these credentials before going to production!");
    }
}
