"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

const NAV_BY_ROLE = {
    SUPER_ADMIN: [
        { label: "Dashboard", href: "/dashboard", icon: "🏠" },
        { label: "Employees", href: "/dashboard/employees", icon: "👥" },
        { label: "Organization", href: "/dashboard/organization", icon: "🏢" },
        { label: "Attendance", href: "/dashboard/attendance", icon: "📅" },
        { label: "Leave", href: "/dashboard/leave", icon: "📝" },
        { label: "Offer Letters", href: "/dashboard/offer-letters", icon: "📄" },
        { label: "Reports", href: "/dashboard/reports", icon: "📊" },
        { label: "Audit Logs", href: "/dashboard/audit-logs", icon: "🔍" },
        { label: "Chat", href: "/dashboard/chat", icon: "💬" },
        { label: "Settings", href: "/dashboard/settings", icon: "⚙️" },
    ],
    HR_ADMIN: [
        { label: "Dashboard", href: "/dashboard", icon: "🏠" },
        { label: "Employees", href: "/dashboard/employees", icon: "👥" },
        { label: "Organization", href: "/dashboard/organization", icon: "🏢" },
        { label: "Attendance", href: "/dashboard/attendance", icon: "📅" },
        { label: "Leave", href: "/dashboard/leave", icon: "📝" },
        { label: "Offer Letters", href: "/dashboard/offer-letters", icon: "📄" },
        { label: "Reports", href: "/dashboard/reports", icon: "📊" },
        { label: "Chat", href: "/dashboard/chat", icon: "💬" },
        { label: "Settings", href: "/dashboard/settings", icon: "⚙️" },
    ],
    TEAM_LEAD: [
        { label: "Dashboard", href: "/dashboard", icon: "🏠" },
        { label: "My Team", href: "/dashboard/employees", icon: "👥" },
        { label: "Organization", href: "/dashboard/organization", icon: "🏢" },
        { label: "Attendance", href: "/dashboard/attendance", icon: "📅" },
        { label: "Leave", href: "/dashboard/leave", icon: "📝" },
        { label: "Chat", href: "/dashboard/chat", icon: "💬" },
    ],
    EMPLOYEE: [
        { label: "Dashboard", href: "/dashboard", icon: "🏠" },
        { label: "My Profile", href: "/dashboard/profile", icon: "👤" },
        { label: "Attendance", href: "/dashboard/attendance", icon: "📅" },
        { label: "Leave", href: "/dashboard/leave", icon: "📝" },
        { label: "Chat", href: "/dashboard/chat", icon: "💬" },
    ],
};

export default function Sidebar({ onClose }) {
    const pathname = usePathname();
    const { user, logout } = useAuth();
    const role = user?.role || "EMPLOYEE";
    const navItems = NAV_BY_ROLE[role] || NAV_BY_ROLE.EMPLOYEE;

    const isActive = (href) =>
        href === "/dashboard" ? pathname === "/dashboard" : pathname.startsWith(href);

    return (
        <aside className="flex flex-col h-full bg-white dark:bg-gray-900 border-r border-gray-100 dark:border-gray-800 w-64">
            {/* Brand */}
            <div className="px-5 py-4 border-b border-gray-100 dark:border-gray-800">
                <Link href="/dashboard" onClick={onClose}>
                    <p className="text-lg font-extrabold text-primary-600">Pravarthana</p>
                    <p className="text-xs text-gray-400 dark:text-gray-500 font-medium tracking-wider">HRMS Platform</p>
                </Link>
            </div>

            {/* User Info */}
            {user && (
                <div className="px-5 py-3 border-b border-gray-100 dark:border-gray-800">
                    <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-primary-700 dark:text-primary-300 font-bold text-sm">
                            {(user.fullName || user.email || "?")[0].toUpperCase()}
                        </div>
                        <div className="min-w-0">
                            <p className="text-sm font-semibold text-gray-900 dark:text-white truncate">
                                {user.fullName || user.email}
                            </p>
                            <p className="text-xs text-gray-400 dark:text-gray-500">{role.replace("_", " ")}</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Nav */}
            <nav className="flex-1 overflow-y-auto py-3 px-3 space-y-0.5">
                {navItems.map((item) => (
                    <Link key={item.href} href={item.href} onClick={onClose}
                        className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 ${isActive(item.href)
                            ? "bg-primary-50 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300"
                            : "text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white"
                            }`}>
                        <span className="text-base">{item.icon}</span>
                        <span>{item.label}</span>
                    </Link>
                ))}
            </nav>

            {/* Logout */}
            <div className="p-3 border-t border-gray-100 dark:border-gray-800">
                <button onClick={logout}
                    className="flex items-center gap-3 w-full px-3 py-2.5 rounded-xl text-sm font-medium text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition">
                    <span>🚪</span>
                    <span>Sign Out</span>
                </button>
            </div>
        </aside>
    );
}
