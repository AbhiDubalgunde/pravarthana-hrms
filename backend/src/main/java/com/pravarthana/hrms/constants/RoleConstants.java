package com.pravarthana.hrms.constants;

/**
 * Centralized role constants for Pravarthana HRMS.
 * Keep in sync with the "roles" table and frontend src/lib/roles.ts
 */
public final class RoleConstants {

    private RoleConstants() { /* utility class */ }

    // ── Role name strings ─────────────────────────────────────────────
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String HR_ADMIN    = "HR_ADMIN";
    public static final String TEAM_LEAD   = "TEAM_LEAD";
    public static final String EMPLOYEE    = "EMPLOYEE";

    // ── Role IDs matching the roles table (Long to match DB SERIAL) ───
    public static final Long SUPER_ADMIN_ID = 1L;
    public static final Long HR_ADMIN_ID    = 2L;
    public static final Long TEAM_LEAD_ID   = 3L;
    public static final Long EMPLOYEE_ID    = 4L;

    /**
     * Converts database role_id (Long) to its role name string.
     * Returns "UNKNOWN" for unrecognized IDs.
     */
    public static String fromRoleId(Long roleId) {
        if (roleId == null) return "UNKNOWN";
        if (roleId.equals(SUPER_ADMIN_ID)) return SUPER_ADMIN;
        if (roleId.equals(HR_ADMIN_ID))    return HR_ADMIN;
        if (roleId.equals(TEAM_LEAD_ID))   return TEAM_LEAD;
        if (roleId.equals(EMPLOYEE_ID))    return EMPLOYEE;
        return "UNKNOWN";
    }
}
