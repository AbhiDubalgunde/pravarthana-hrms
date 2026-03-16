"use client";

import { useEffect, useState, useCallback } from "react";
import { useAuth } from "@/context/AuthContext";
import { useToast } from "@/context/ToastContext";
import api from "@/services/api";

const LEAVE_TYPES = ["CASUAL", "SICK", "EARNED", "UNPAID"];

const STATUS_CONFIG = {
    PENDING: { cls: "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300", label: "Pending" },
    APPROVED: { cls: "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300", label: "Approved" },
    REJECTED: { cls: "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300", label: "Rejected" },
    CANCELLED: { cls: "bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400", label: "Cancelled" },
};

function Badge({ status }) {
    const cfg = STATUS_CONFIG[status] || STATUS_CONFIG.PENDING;
    return <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${cfg.cls}`}>{cfg.label}</span>;
}

export default function LeavePage() {
    const { user } = useAuth();
    const toast = useToast();
    const role = user?.role || "EMPLOYEE";
    const isHR = role === "SUPER_ADMIN" || role === "HR_ADMIN";

    const [tab, setTab] = useState("my");          // "my" | "pending" (HR only)
    const [myLeaves, setMyLeaves] = useState([]);
    const [pending, setPending] = useState([]);
    const [loading, setLoading] = useState(true);
    const [applying, setApplying] = useState(false);
    const [actionId, setActionId] = useState(null);  // leave id being approved/rejected
    const [showForm, setShowForm] = useState(false);
    const [form, setForm] = useState({ leaveType: "CASUAL", startDate: "", endDate: "", reason: "" });

    const loadMyLeaves = useCallback(async () => {
        try {
            const r = await api.get("/leaves/my");
            setMyLeaves(Array.isArray(r.data) ? r.data : []);
        } catch { setMyLeaves([]); }
    }, []);

    const loadPending = useCallback(async () => {
        try {
            const r = await api.get("/leaves/pending");
            setPending(r.data?.content || []);
        } catch { setPending([]); }
    }, []);

    useEffect(() => {
        Promise.all([
            loadMyLeaves(),
            isHR ? loadPending() : Promise.resolve()
        ]).finally(() => setLoading(false));
    }, [isHR]);

    const handleApply = async (e) => {
        e.preventDefault();
        if (!form.startDate || !form.endDate) return toast.error("Start and end dates are required");
        setApplying(true);
        try {
            await api.post("/leaves", form);
            toast.success("Leave applied successfully! ✅");
            setShowForm(false);
            setForm({ leaveType: "CASUAL", startDate: "", endDate: "", reason: "" });
            loadMyLeaves();
        } catch (err) {
            toast.error(err.response?.data?.error || err.response?.data?.message || "Failed to apply leave");
        } finally { setApplying(false); }
    };

    const handleApprove = async (id) => {
        setActionId(id);
        try {
            await api.put(`/leaves/${id}/approve`);
            toast.success("Leave approved ✅");
            loadPending();
            loadMyLeaves();
        } catch (err) {
            toast.error(err.response?.data?.error || "Failed to approve");
        } finally { setActionId(null); }
    };

    const handleReject = async (id) => {
        const reason = prompt("Rejection reason (optional):");
        if (reason === null) return;
        setActionId(id);
        try {
            await api.put(`/leaves/${id}/reject`, { reason });
            toast.success("Leave rejected");
            loadPending();
        } catch (err) {
            toast.error(err.response?.data?.error || "Failed to reject");
        } finally { setActionId(null); }
    };

    const days = form.startDate && form.endDate
        ? Math.max(0, Math.round((new Date(form.endDate) - new Date(form.startDate)) / 86400000) + 1)
        : 0;

    return (
        <div>
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Leave Management</h1>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">Apply and track leave requests</p>
                </div>
                <button onClick={() => setShowForm(v => !v)}
                    className="inline-flex items-center gap-2 bg-primary-600 text-white px-4 py-2.5 rounded-xl font-medium hover:bg-primary-700 transition shadow-sm text-sm">
                    {showForm ? "✕ Cancel" : "+ Apply Leave"}
                </button>
            </div>

            {/* Apply Leave Form */}
            {showForm && (
                <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 p-6 mb-6">
                    <h2 className="font-semibold text-gray-900 dark:text-white mb-4">Apply for Leave</h2>
                    <form onSubmit={handleApply} className="space-y-4">
                        <div className="grid sm:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Leave Type</label>
                                <select value={form.leaveType} onChange={e => setForm(f => ({ ...f, leaveType: e.target.value }))}
                                    className="w-full px-3 py-2.5 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none">
                                    {LEAVE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                                </select>
                            </div>
                            <div className="flex items-end gap-2">
                                <div className="flex-1">
                                    <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">From</label>
                                    <input type="date" required value={form.startDate}
                                        onChange={e => setForm(f => ({ ...f, startDate: e.target.value }))}
                                        className="w-full px-3 py-2.5 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                                </div>
                                <div className="flex-1">
                                    <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">To</label>
                                    <input type="date" required value={form.endDate}
                                        onChange={e => setForm(f => ({ ...f, endDate: e.target.value }))}
                                        min={form.startDate}
                                        className="w-full px-3 py-2.5 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                                </div>
                                {days > 0 && (
                                    <div className="px-3 py-2.5 bg-primary-50 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300 rounded-xl text-sm font-semibold whitespace-nowrap">
                                        {days}d
                                    </div>
                                )}
                            </div>
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Reason</label>
                            <textarea rows={3} value={form.reason}
                                onChange={e => setForm(f => ({ ...f, reason: e.target.value }))}
                                placeholder="Brief reason for leave (optional)"
                                className="w-full px-3 py-2.5 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none resize-none" />
                        </div>
                        <button type="submit" disabled={applying}
                            className="bg-primary-600 text-white px-6 py-2.5 rounded-xl font-semibold hover:bg-primary-700 disabled:opacity-50 transition text-sm">
                            {applying ? "Submitting…" : "Submit Leave Request"}
                        </button>
                    </form>
                </div>
            )}

            {/* Tabs (HR only) */}
            {isHR && (
                <div className="flex gap-2 mb-5">
                    {[["my", "My Leaves"], ["pending", `Pending Approvals ${pending.length > 0 ? `(${pending.length})` : ""}`]].map(([k, l]) => (
                        <button key={k} onClick={() => setTab(k)}
                            className={`px-4 py-2 rounded-xl text-sm font-medium transition ${tab === k
                                ? "bg-primary-600 text-white shadow-sm"
                                : "bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700"}`}>
                            {l}
                        </button>
                    ))}
                </div>
            )}

            {/* Tables */}
            {loading ? (
                <div className="space-y-3">
                    {[1, 2, 3].map(i => <div key={i} className="h-14 bg-gray-100 dark:bg-gray-800 rounded-xl animate-pulse" />)}
                </div>
            ) : tab === "my" ? (
                <LeaveTable leaves={myLeaves} emptyMsg="No leave records found. Apply your first leave!" />
            ) : (
                <PendingTable leaves={pending} onApprove={handleApprove} onReject={handleReject} actionId={actionId} />
            )}
        </div>
    );
}

function LeaveTable({ leaves, emptyMsg }) {
    if (leaves.length === 0) return (
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 p-16 text-center text-gray-400">
            <p className="text-4xl mb-3">📝</p>
            <p className="font-medium">{emptyMsg}</p>
        </div>
    );
    return (
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 overflow-hidden">
            <div className="overflow-x-auto">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 dark:bg-gray-700/50">
                        <tr className="text-left text-gray-500 dark:text-gray-400">
                            {["Type", "From", "To", "Days", "Reason", "Status"].map(h => (
                                <th key={h} className="px-5 py-3.5 font-medium text-xs uppercase tracking-wider">{h}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50 dark:divide-gray-700">
                        {leaves.map(l => (
                            <tr key={l.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition">
                                <td className="px-5 py-4 font-medium text-gray-900 dark:text-white">{l.leaveType}</td>
                                <td className="px-5 py-4 text-gray-600 dark:text-gray-300">{l.startDate}</td>
                                <td className="px-5 py-4 text-gray-600 dark:text-gray-300">{l.endDate}</td>
                                <td className="px-5 py-4 text-gray-600 dark:text-gray-300">{l.days}</td>
                                <td className="px-5 py-4 text-gray-500 dark:text-gray-400 max-w-[180px] truncate">{l.reason || "—"}</td>
                                <td className="px-5 py-4"><Badge status={l.status} /></td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

function PendingTable({ leaves, onApprove, onReject, actionId }) {
    if (leaves.length === 0) return (
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 p-16 text-center text-gray-400">
            <p className="text-4xl mb-3">✅</p>
            <p className="font-medium">No pending leave requests! All clear.</p>
        </div>
    );
    return (
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 overflow-hidden">
            <div className="overflow-x-auto">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 dark:bg-gray-700/50">
                        <tr className="text-left text-gray-500 dark:text-gray-400">
                            {["Employee", "Type", "From", "To", "Days", "Reason", "Actions"].map(h => (
                                <th key={h} className="px-5 py-3.5 font-medium text-xs uppercase tracking-wider">{h}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50 dark:divide-gray-700">
                        {leaves.map(l => (
                            <tr key={l.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition">
                                <td className="px-5 py-4 font-medium text-gray-900 dark:text-white">{l.employeeName || `Emp #${l.employeeId}`}</td>
                                <td className="px-5 py-4 text-gray-600 dark:text-gray-300">{l.leaveType}</td>
                                <td className="px-5 py-4 text-gray-600 dark:text-gray-300">{l.startDate}</td>
                                <td className="px-5 py-4 text-gray-600 dark:text-gray-300">{l.endDate}</td>
                                <td className="px-5 py-4 text-gray-600 dark:text-gray-300">{l.days}</td>
                                <td className="px-5 py-4 text-gray-500 dark:text-gray-400 max-w-[140px] truncate">{l.reason || "—"}</td>
                                <td className="px-5 py-4">
                                    <div className="flex gap-2">
                                        <button onClick={() => onApprove(l.id)} disabled={actionId === l.id}
                                            className="px-3 py-1.5 text-xs font-medium rounded-lg bg-emerald-600 text-white hover:bg-emerald-700 disabled:opacity-40 transition">
                                            {actionId === l.id ? "…" : "Approve"}
                                        </button>
                                        <button onClick={() => onReject(l.id)} disabled={actionId === l.id}
                                            className="px-3 py-1.5 text-xs font-medium rounded-lg border border-red-300 dark:border-red-700 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 disabled:opacity-40 transition">
                                            Reject
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
