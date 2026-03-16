/**
 * Centralized role constants for Pravarthana HRMS frontend.
 * Must stay in sync with backend RoleConstants.java
 */
export const ROLES = {
    SUPER_ADMIN: 'SUPER_ADMIN',
    HR_ADMIN: 'HR_ADMIN',
    TEAM_LEAD: 'TEAM_LEAD',
    EMPLOYEE: 'EMPLOYEE',
};

export const ROLE_LABELS = {
    SUPER_ADMIN: 'Super Admin',
    HR_ADMIN: 'HR Admin',
    TEAM_LEAD: 'Team Lead',
    EMPLOYEE: 'Employee',
};

export const NAV_ITEMS = {
    SUPER_ADMIN: [
        { label: 'Dashboard', href: '/dashboard', icon: '🏠' },
        { label: 'Employees', href: '/dashboard/employees', icon: '👥' },
        { label: 'Departments', href: '/dashboard/departments', icon: '🏢' },
        { label: 'Attendance', href: '/dashboard/attendance', icon: '📅' },
        { label: 'Leave Requests', href: '/dashboard/leave', icon: '📝' },
        { label: 'Payroll', href: '/dashboard/payroll', icon: '💰' },
        { label: 'Reports', href: '/dashboard/reports', icon: '📊' },
        { label: 'Settings', href: '/dashboard/settings', icon: '⚙️' },
    ],
    HR_ADMIN: [
        { label: 'Dashboard', href: '/dashboard', icon: '🏠' },
        { label: 'Employees', href: '/dashboard/employees', icon: '👥' },
        { label: 'Attendance', href: '/dashboard/attendance', icon: '📅' },
        { label: 'Leave Requests', href: '/dashboard/leave', icon: '📝' },
        { label: 'Payroll', href: '/dashboard/payroll', icon: '💰' },
        { label: 'Reports', href: '/dashboard/reports', icon: '📊' },
    ],
    TEAM_LEAD: [
        { label: 'Dashboard', href: '/dashboard', icon: '🏠' },
        { label: 'My Team', href: '/dashboard/team', icon: '👥' },
        { label: 'Attendance', href: '/dashboard/attendance', icon: '📅' },
        { label: 'Leave Requests', href: '/dashboard/leave', icon: '📝' },
    ],
    EMPLOYEE: [
        { label: 'Dashboard', href: '/dashboard', icon: '🏠' },
        { label: 'My Profile', href: '/dashboard/profile', icon: '👤' },
        { label: 'My Attendance', href: '/dashboard/attendance', icon: '📅' },
        { label: 'Apply Leave', href: '/dashboard/leave', icon: '📝' },
    ],
};

export function hasAccess(userRole, requiredRole) {
    const hierarchy = [ROLES.EMPLOYEE, ROLES.TEAM_LEAD, ROLES.HR_ADMIN, ROLES.SUPER_ADMIN];
    const userIndex = hierarchy.indexOf(userRole);
    const requiredIndex = hierarchy.indexOf(requiredRole);
    return userIndex >= requiredIndex;
}
