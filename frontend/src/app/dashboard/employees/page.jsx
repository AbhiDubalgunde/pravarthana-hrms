"use client";

import { useEffect, useState, useCallback } from "react";
import { useAuth } from "@/context/AuthContext";
import { useToast } from "@/context/ToastContext";
import { employeeService } from "@/services/api";
import { departmentService } from "@/services/departmentService";

function Modal({ open, onClose, title, children }) {
    if (!open) return null;
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-black/50" onClick={onClose} />
            <div className="relative bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-lg p-6 z-10 max-h-[90vh] overflow-y-auto">
                <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-bold text-gray-900 dark:text-white">{title}</h3>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                    </button>
                </div>
                {children}
            </div>
        </div>
    );
}

function EmployeeForm({ initial, onSubmit, loading }) {
    const [form, setForm] = useState(() => {
        const base = initial || {};
        return {
            firstName: base.firstName || "",
            lastName: base.lastName || "",
            email: base.email || "",
            phone: base.phone || "",
            departmentId: base.departmentId ? Number(base.departmentId) : "",
            designation: base.designation || "",
            joiningDate: base.joiningDate || "",
            status: base.status || "ACTIVE",
        };
    });

    // Departments fetched directly inside the form so Create and Edit always work
    const [depts, setDepts] = useState([]);
    const [deptsLoading, setDeptsLoading] = useState(true);
    const [deptsError, setDeptsError] = useState(null);

    useEffect(() => {
        setDeptsLoading(true);
        departmentService.getAll()
            .then(r => {
                const list = r.data || [];
                setDepts(list);
                // For Create mode: auto-select first dept
                setForm(f => ({
                    ...f,
                    departmentId: f.departmentId || (list[0]?.id ?? ""),
                }));
            })
            .catch(() => setDeptsError("Could not load departments"))
            .finally(() => setDeptsLoading(false));
    }, []);

    const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

    const textFields = [
        { key: "firstName", label: "First Name", type: "text", req: true },
        { key: "lastName", label: "Last Name", type: "text" },
        { key: "email", label: "Email", type: "email", req: true },
        { key: "phone", label: "Phone", type: "tel" },
        { key: "designation", label: "Designation", type: "text" },
        { key: "joiningDate", label: "Joining Date", type: "date" },
    ];

    return (
        <form onSubmit={(e) => { e.preventDefault(); onSubmit(form); }} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
                {textFields.map(({ key, label, type, req }) => (
                    <div key={key} className={key === "email" || key === "designation" ? "col-span-2" : ""}>
                        <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">{label}{req && " *"}</label>
                        <input type={type} value={form[key] || ""} required={req}
                            onChange={(e) => set(key, e.target.value)}
                            className="w-full px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                    </div>
                ))}

                {/* Department — fetched by the form itself */}
                <div>
                    <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Department *</label>
                    {deptsLoading ? (
                        <div className="w-full px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-xl text-sm text-gray-400">Loading departments…</div>
                    ) : deptsError ? (
                        <div className="text-xs text-red-500 py-2">{deptsError}</div>
                    ) : (
                        <select
                            value={form.departmentId || ""}
                            onChange={(e) => set("departmentId", e.target.value ? Number(e.target.value) : "")}
                            required
                            className="w-full px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none"
                        >
                            <option value="">Select department…</option>
                            {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                        </select>
                    )}
                </div>

                {/* Status */}
                <div>
                    <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Status</label>
                    <select value={form.status || "ACTIVE"} onChange={(e) => set("status", e.target.value)}
                        className="w-full px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none">
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                    </select>
                </div>
            </div>
            <button type="submit" disabled={loading}
                className="w-full bg-primary-600 text-white py-2.5 rounded-xl font-semibold hover:bg-primary-700 disabled:opacity-50 transition">
                {loading ? "Saving…" : "Save Employee"}
            </button>
        </form>
    );
}

export default function EmployeesPage() {
    const { user } = useAuth();
    const toast = useToast();
    const role = user?.role || "EMPLOYEE";
    const canEdit = role === "SUPER_ADMIN" || role === "HR_ADMIN";
    const canDelete = role === "SUPER_ADMIN";

    const [employees, setEmployees] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [page, setPage] = useState(0);
    const [search, setSearch] = useState("");
    const [deptId, setDeptId] = useState("");
    const [loading, setLoading] = useState(true);
    const [modalMode, setModalMode] = useState(null);
    const [selected, setSelected] = useState(null);
    const [saving, setSaving] = useState(false);
    const [deleting, setDeleting] = useState(null);

    // Fetch departments once (for filter dropdown + form)
    useEffect(() => {
        departmentService.getAll()
            .then(r => setDepartments(r.data || []))
            .catch(() => { }); // non-fatal
    }, []);

    const load = useCallback(async () => {
        setLoading(true);
        try {
            let res;
            if (role === "TEAM_LEAD") {
                res = await employeeService.getMyTeam();
                setEmployees(res.data);
                setTotalPages(1);
            } else {
                res = await employeeService.getAll({ search, departmentId: deptId, page, size: 15 });
                setEmployees(res.data.content || []);
                setTotalPages(res.data.totalPages || 0);
            }
        } catch {
            toast.error("Failed to load employees");
        } finally { setLoading(false); }
    }, [role, search, deptId, page]);

    useEffect(() => { load(); }, [load]);

    const handleCreate = async (form) => {
        setSaving(true);
        try {
            await employeeService.create(form);
            toast.success("Employee created successfully! ✅");
            setModalMode(null);
            load();
        } catch (err) {
            toast.error(err.response?.data?.error || "Failed to create employee");
        } finally { setSaving(false); }
    };

    const handleUpdate = async (form) => {
        setSaving(true);
        try {
            await employeeService.update(selected.id, form);
            toast.success("Employee updated successfully! ✅");
            setModalMode(null);
            setSelected(null);
            load();
        } catch (err) {
            toast.error(err.response?.data?.error || "Failed to update employee");
        } finally { setSaving(false); }
    };

    const handleDelete = async (emp) => {
        if (!confirm(`Delete ${emp.fullName || emp.firstName}? This cannot be undone.`)) return;
        setDeleting(emp.id);
        try {
            await employeeService.delete(emp.id);
            toast.success("Employee deleted.");
            load();
        } catch {
            toast.error("Failed to delete employee");
        } finally { setDeleting(null); }
    };

    return (
        <div>
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                        {role === "TEAM_LEAD" ? "My Team" : "Employees"}
                    </h1>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">Manage your workforce</p>
                </div>
                {canEdit && (
                    <button onClick={() => setModalMode("create")}
                        className="inline-flex items-center gap-2 bg-primary-600 text-white px-4 py-2.5 rounded-xl font-medium hover:bg-primary-700 transition shadow-sm text-sm">
                        <span>+</span> Add Employee
                    </button>
                )}
            </div>

            {/* Filters */}
            <div className="flex flex-col sm:flex-row gap-3 mb-5">
                <input type="text" placeholder="Search by name, email, code…" value={search}
                    onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                    className="flex-1 px-4 py-2.5 border border-gray-200 dark:border-gray-700 rounded-xl text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                <select value={deptId} onChange={(e) => { setDeptId(e.target.value); setPage(0); }}
                    className="px-4 py-2.5 border border-gray-200 dark:border-gray-700 rounded-xl text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none">
                    <option value="">All Departments</option>
                    {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
            </div>

            {/* Table */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 overflow-hidden">
                {loading ? (
                    <div className="p-6 space-y-3">
                        {[1, 2, 3, 4, 5].map(i => <div key={i} className="h-12 bg-gray-100 dark:bg-gray-700 rounded-xl animate-pulse" />)}
                    </div>
                ) : employees.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <p className="text-5xl mb-3">👥</p>
                        <p className="font-medium">No employees found</p>
                        {canEdit && <p className="text-sm mt-1">Click "Add Employee" to get started</p>}
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="bg-gray-50 dark:bg-gray-700/50">
                                <tr className="text-left text-gray-500 dark:text-gray-400">
                                    {["Employee", "Department", "Designation", "Joined", "Status", "Actions"].map(h => (
                                        <th key={h} className="px-5 py-3.5 font-medium text-xs uppercase tracking-wider">{h}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50 dark:divide-gray-700">
                                {employees.map((emp) => (
                                    <tr key={emp.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition">
                                        <td className="px-5 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-9 h-9 rounded-full bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-primary-700 dark:text-primary-300 font-bold text-sm flex-shrink-0">
                                                    {(emp.firstName || "?")[0].toUpperCase()}
                                                </div>
                                                <div>
                                                    <a href={`/dashboard/employees/${emp.id}`}
                                                        className="font-semibold text-gray-900 dark:text-white hover:text-primary-600 transition">
                                                        {emp.fullName || `${emp.firstName} ${emp.lastName || ""}`.trim()}
                                                    </a>
                                                    <p className="text-xs text-gray-400">{emp.employeeCode} · {emp.email}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-5 py-4 text-gray-600 dark:text-gray-300">
                                            {emp.departmentName || emp.department || "—"}
                                        </td>
                                        <td className="px-5 py-4 text-gray-600 dark:text-gray-300">{emp.designation || "—"}</td>
                                        <td className="px-5 py-4 text-gray-500 dark:text-gray-400 text-xs">{emp.joiningDate || "—"}</td>
                                        <td className="px-5 py-4">
                                            <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${emp.status === "ACTIVE"
                                                ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300"
                                                : "bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400"
                                                }`}>{emp.status || "ACTIVE"}</span>
                                        </td>
                                        <td className="px-5 py-4">
                                            <div className="flex items-center gap-2">
                                                <a href={`/dashboard/employees/${emp.id}`}
                                                    className="text-xs px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-600 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition">
                                                    View
                                                </a>
                                                {canEdit && (
                                                    <button onClick={() => { setSelected(emp); setModalMode("edit"); }}
                                                        className="text-xs px-3 py-1.5 rounded-lg border border-primary-200 dark:border-primary-800 text-primary-600 dark:text-primary-400 hover:bg-primary-50 dark:hover:bg-primary-900/20 transition">
                                                        Edit
                                                    </button>
                                                )}
                                                {canDelete && (
                                                    <button onClick={() => handleDelete(emp)} disabled={deleting === emp.id}
                                                        className="text-xs px-3 py-1.5 rounded-lg border border-red-200 dark:border-red-800 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition disabled:opacity-40">
                                                        {deleting === emp.id ? "…" : "Delete"}
                                                    </button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {/* Pagination */}
                {totalPages > 1 && (
                    <div className="flex items-center justify-between px-5 py-3 border-t border-gray-100 dark:border-gray-700">
                        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                            className="px-4 py-2 text-sm rounded-lg border border-gray-200 dark:border-gray-600 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-gray-700 transition">
                            ← Prev
                        </button>
                        <span className="text-sm text-gray-500">Page {page + 1} of {totalPages}</span>
                        <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}
                            className="px-4 py-2 text-sm rounded-lg border border-gray-200 dark:border-gray-600 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-gray-700 transition">
                            Next →
                        </button>
                    </div>
                )}
            </div>

            {/* Create Modal */}
            <Modal open={modalMode === "create"} onClose={() => setModalMode(null)} title="Add New Employee">
                <EmployeeForm onSubmit={handleCreate} loading={saving} />
            </Modal>

            {/* Edit Modal */}
            <Modal open={modalMode === "edit"} onClose={() => { setModalMode(null); setSelected(null); }} title="Edit Employee">
                {selected && <EmployeeForm initial={selected} onSubmit={handleUpdate} loading={saving} />}
            </Modal>
        </div>
    );
}
