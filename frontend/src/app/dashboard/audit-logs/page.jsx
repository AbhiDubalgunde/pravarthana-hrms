"use client";

import { useEffect, useState, useCallback } from "react";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import api from "@/services/api";

function fmtDate(dt) {
    if (!dt) return "—";
    return new Date(dt).toLocaleString("en-IN", { dateStyle: "medium", timeStyle: "short" });
}

const ACTION_COLORS = {
    LOGIN_SUCCESS: "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300",
    LOGIN_FAILED: "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300",
    EMPLOYEE_CREATED: "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300",
    EMPLOYEE_UPDATED: "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300",
    EMPLOYEE_DELETED: "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300",
};

export default function AuditLogsPage() {
    const { user } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (user?.role && user.role !== "SUPER_ADMIN") router.replace("/dashboard");
    }, [user?.role]);

    const [logs, setLogs] = useState([]);
    const [total, setTotal] = useState(0);
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({ action: "", email: "", startDate: "", endDate: "" });
    const setF = (k, v) => setFilters(f => ({ ...f, [k]: v }));

    const load = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({ page: String(page), size: "30" });
            if (filters.action) params.set("action", filters.action);
            if (filters.email) params.set("email", filters.email);
            if (filters.startDate) params.set("startDate", filters.startDate);
            if (filters.endDate) params.set("endDate", filters.endDate);
            const r = await api.get(`/audit-logs?${params.toString()}`);
            setLogs(r.data?.content || []);
            setTotal(r.data?.totalElements || 0);
        } catch { setLogs([]); }
        setLoading(false);
    }, [page, filters]);

    useEffect(() => { load(); }, [load]);

    return (
        <div>
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Audit Logs</h1>
                <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">Company-wide activity trail — Admin only</p>
            </div>

            {/* Filters */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 p-4 mb-5">
                <div className="grid sm:grid-cols-4 gap-3">
                    <input placeholder="Filter by action…" value={filters.action}
                        onChange={e => { setF("action", e.target.value); setPage(0); }}
                        className="px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                    <input placeholder="Filter by email…" value={filters.email}
                        onChange={e => { setF("email", e.target.value); setPage(0); }}
                        className="px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                    <input type="date" value={filters.startDate}
                        onChange={e => { setF("startDate", e.target.value); setPage(0); }}
                        className="px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                    <input type="date" value={filters.endDate}
                        onChange={e => { setF("endDate", e.target.value); setPage(0); }}
                        className="px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                </div>
            </div>

            {/* Log table */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 overflow-hidden">
                {loading ? (
                    <div className="p-6 space-y-3">
                        {[1, 2, 3, 4, 5].map(i => <div key={i} className="h-10 bg-gray-100 dark:bg-gray-700 rounded-xl animate-pulse" />)}
                    </div>
                ) : logs.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <p className="text-4xl mb-3">🔍</p>
                        <p className="font-medium">No audit logs found</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="bg-gray-50 dark:bg-gray-700/50">
                                <tr className="text-left text-gray-500 dark:text-gray-400">
                                    {["Time", "Action", "User", "Entity", "IP", "Request ID"].map(h => (
                                        <th key={h} className="px-4 py-3.5 font-medium text-xs uppercase tracking-wider">{h}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50 dark:divide-gray-700">
                                {logs.map(log => (
                                    <tr key={log.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition">
                                        <td className="px-4 py-3 text-gray-500 dark:text-gray-400 text-xs whitespace-nowrap">{fmtDate(log.createdAt)}</td>
                                        <td className="px-4 py-3">
                                            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${ACTION_COLORS[log.action] || "bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300"}`}>
                                                {log.action}
                                            </span>
                                        </td>
                                        <td className="px-4 py-3 text-gray-600 dark:text-gray-300 text-xs">{log.performedBy || `User #${log.userId}`}</td>
                                        <td className="px-4 py-3 text-gray-500 dark:text-gray-400 text-xs">{log.entityType ? `${log.entityType} #${log.entityId}` : "—"}</td>
                                        <td className="px-4 py-3 text-gray-400 dark:text-gray-500 text-xs font-mono">{log.ipAddress || "—"}</td>
                                        <td className="px-4 py-3 text-gray-400 dark:text-gray-500 text-xs font-mono truncate max-w-[120px]">{log.requestId || "—"}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Pagination */}
            {total > 30 && (
                <div className="flex items-center justify-between mt-4">
                    <p className="text-sm text-gray-500">{total} total logs</p>
                    <div className="flex gap-2">
                        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                            className="px-3 py-1.5 text-sm rounded-lg border border-gray-200 dark:border-gray-600 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-gray-700 transition">← Prev</button>
                        <button disabled={(page + 1) * 30 >= total} onClick={() => setPage(p => p + 1)}
                            className="px-3 py-1.5 text-sm rounded-lg border border-gray-200 dark:border-gray-600 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-gray-700 transition">Next →</button>
                    </div>
                </div>
            )}
        </div>
    );
}
