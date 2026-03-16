"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import api from "@/services/api";
import { departmentService } from "@/services/departmentService";

/* ── Employee leaf node ──────────────────────────────────── */
function EmployeeNode({ emp }) {
    const router = useRouter();
    const initials = ((emp.firstName || "?")[0] + (emp.lastName || " ")[0]).toUpperCase();
    return (
        <div
            className="group flex items-center gap-3 py-2 px-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800/60 transition-all duration-150 cursor-pointer"
            onClick={() => router.push(`/dashboard/employees/${emp.id}`)}
        >
            <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-primary-700 dark:text-primary-300 font-bold text-xs flex-shrink-0">
                {initials}
            </div>
            <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{emp.name}</p>
                <p className="text-xs text-gray-500 dark:text-gray-400 truncate">{emp.designation || "—"}</p>
            </div>
            {emp.employeeCode && (
                <span className="hidden sm:block text-xs font-mono bg-gray-100 dark:bg-gray-800 text-gray-400 px-2 py-0.5 rounded">
                    {emp.employeeCode}
                </span>
            )}
            <button
                onClick={(e) => { e.stopPropagation(); router.push(`/dashboard/employees/${emp.id}`); }}
                className="opacity-0 group-hover:opacity-100 transition text-xs text-primary-600 dark:text-primary-400 hover:underline flex-shrink-0"
            >
                Profile →
            </button>
        </div>
    );
}

/* ── Team section within a department ───────────────────── */
function TeamSection({ team }) {
    const [expanded, setExpanded] = useState(true);
    const empCount = team.employees?.length ?? 0;
    return (
        <div className="ml-4 mt-2 border-l-2 border-gray-100 dark:border-gray-800 pl-4">
            <button
                className="flex items-center gap-2 text-sm font-semibold text-gray-700 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400 py-1.5 transition"
                onClick={() => setExpanded(!expanded)}
            >
                <svg className={`w-3.5 h-3.5 text-gray-400 transition-transform duration-200 ${expanded ? "rotate-90" : ""}`}
                    fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
                <span className="text-indigo-400">⬡</span>
                {team.name}
                <span className="text-xs font-normal text-gray-400 bg-gray-100 dark:bg-gray-800 px-2 py-0.5 rounded-full">
                    {empCount} member{empCount !== 1 ? "s" : ""}
                </span>
            </button>
            {expanded && (
                <div className="mt-1 space-y-0.5">
                    {empCount === 0 ? (
                        <p className="text-xs text-gray-400 dark:text-gray-600 px-3 py-1 italic">No employees in this team</p>
                    ) : (
                        team.employees.map((emp) => <EmployeeNode key={emp.id} emp={emp} />)
                    )}
                </div>
            )}
        </div>
    );
}

/* ── Department card ─────────────────────────────────────── */
function DepartmentCard({ dept }) {
    const [expanded, setExpanded] = useState(true);
    const totalEmps =
        (dept.employees?.length ?? 0) +
        (dept.teams ?? []).reduce((s, t) => s + (t.employees?.length ?? 0), 0);

    return (
        <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-100 dark:border-gray-800 shadow-sm overflow-hidden">
            {/* Dept header */}
            <button
                className="w-full flex items-center gap-3 px-5 py-4 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition text-left"
                onClick={() => setExpanded(!expanded)}
            >
                <div className="w-10 h-10 rounded-xl bg-primary-600 flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                    {dept.name[0]}
                </div>
                <div className="flex-1 min-w-0">
                    <p className="font-bold text-gray-900 dark:text-white">{dept.name}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                        {totalEmps} employee{totalEmps !== 1 ? "s" : ""}
                        {dept.teams?.length > 0 && ` · ${dept.teams.length} team${dept.teams.length !== 1 ? "s" : ""}`}
                    </p>
                </div>
                <svg className={`w-5 h-5 text-gray-400 transition-transform duration-200 flex-shrink-0 ${expanded ? "rotate-180" : ""}`}
                    fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
            </button>

            {/* Dept body */}
            {expanded && (
                <div className="px-3 pb-3 space-y-0.5 border-t border-gray-50 dark:border-gray-800 pt-2">
                    {/* Teams */}
                    {dept.teams?.map((team) => (
                        <TeamSection key={team.id} team={team} />
                    ))}

                    {/* Employees not in any team */}
                    {dept.employees?.length > 0 && (
                        <div className={dept.teams?.length > 0 ? "ml-4 mt-2 border-l-2 border-dashed border-gray-100 dark:border-gray-800 pl-4" : ""}>
                            {dept.teams?.length > 0 && (
                                <p className="text-xs text-gray-400 dark:text-gray-600 py-1 px-3 italic">No team assigned</p>
                            )}
                            {dept.employees.map((emp) => <EmployeeNode key={emp.id} emp={emp} />)}
                        </div>
                    )}

                    {/* Empty state */}
                    {totalEmps === 0 && (
                        <p className="text-sm text-gray-400 dark:text-gray-600 text-center py-4 italic">
                            No employees in this department
                        </p>
                    )}
                </div>
            )}
        </div>
    );
}

/* ── Main page ───────────────────────────────────────────── */
export default function OrganizationPage() {
    const [tree, setTree] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [search, setSearch] = useState("");

    useEffect(() => {
        api.get("/employees/org-structure")
            .then((r) => setTree(r.data))
            .catch((e) => setError(e?.response?.data?.message || "Failed to load org structure"))
            .finally(() => setLoading(false));
    }, []);

    const totalEmployees = tree.reduce((sum, dept) => {
        const inTeams = (dept.teams || []).reduce((s, t) => s + (t.employees?.length ?? 0), 0);
        return sum + (dept.employees?.length ?? 0) + inTeams;
    }, 0);

    // Filter helper
    const filterTree = (nodes, q) => {
        if (!q) return nodes;
        const lq = q.toLowerCase();
        return nodes.map(dept => {
            const filteredTeams = (dept.teams || []).map(team => {
                const filteredEmps = (team.employees || []).filter(e =>
                    e.name?.toLowerCase().includes(lq) || e.designation?.toLowerCase().includes(lq));
                return { ...team, employees: filteredEmps };
            }).filter(t => t.employees.length > 0);

            const filteredDeptEmps = (dept.employees || []).filter(e =>
                e.name?.toLowerCase().includes(lq) || e.designation?.toLowerCase().includes(lq));

            if (filteredTeams.length > 0 || filteredDeptEmps.length > 0 || dept.name?.toLowerCase().includes(lq)) {
                return { ...dept, teams: filteredTeams, employees: filteredDeptEmps };
            }
            return null;
        }).filter(Boolean);
    };

    const displayTree = filterTree(tree, search);

    return (
        <div className="max-w-5xl mx-auto space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Organization Structure</h1>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
                        {tree.length} department{tree.length !== 1 ? "s" : ""} · {totalEmployees} employee{totalEmployees !== 1 ? "s" : ""}
                    </p>
                </div>
                <div className="relative w-full sm:w-72">
                    <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
                        fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                    <input
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        placeholder="Search by name, role…"
                        className="w-full pl-9 pr-3 py-2 text-sm bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                </div>
            </div>

            {/* Tree */}
            {loading ? (
                <div className="flex items-center justify-center py-16">
                    <div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" />
                </div>
            ) : error ? (
                <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-2xl p-6 text-center">{error}</div>
            ) : displayTree.length === 0 ? (
                <div className="text-center py-16 text-gray-500 dark:text-gray-400">
                    {search ? "No employees match your search." : "No departments found."}
                </div>
            ) : (
                <div className="space-y-4">
                    {displayTree.map((dept) => (
                        <DepartmentCard key={dept.id} dept={dept} />
                    ))}
                </div>
            )}

            {/* Legend */}
            <div className="flex flex-wrap items-center gap-4 text-xs text-gray-500 dark:text-gray-400 px-1">
                <div className="flex items-center gap-1.5">
                    <div className="w-3 h-3 rounded bg-primary-600" />
                    Department
                </div>
                <div className="flex items-center gap-1.5">
                    <span className="text-indigo-400">⬡</span>
                    Team
                </div>
                <div className="flex items-center gap-1.5">
                    <div className="w-3 h-3 rounded-full bg-primary-100 dark:bg-primary-900/40" />
                    Employee (click to view profile)
                </div>
            </div>
        </div>
    );
}
