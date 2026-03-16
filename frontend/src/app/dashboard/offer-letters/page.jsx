"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { useToast } from "@/context/ToastContext";
import { useRouter } from "next/navigation";
import api from "@/services/api";

const DEPARTMENTS = ["Engineering", "HR", "Finance", "Operations", "Sales", "Marketing", "Support", "Management"];

function ctcBreakdown(annual) {
    if (!annual || isNaN(annual)) return null;
    const a = Number(annual);
    const basic = a * 0.40;
    const hra = a * 0.20;
    const special = a * 0.30;
    const pfEmp = a * 0.0481;
    const pfEmpl = a * 0.0481;
    const pt = 2400;
    const gross = basic + hra + special;
    const net = (a - pfEmpl - pt) / 12;
    return { basic, hra, special, gross, pfEmp, pfEmpl, pt, net, total: a };
}

function fmtINR(n) {
    return n == null ? "—" : `₹${Number(n).toLocaleString("en-IN", { maximumFractionDigits: 0 })}`;
}

export default function OfferLettersPage() {
    const { user } = useAuth();
    const toast = useToast();
    const router = useRouter();
    const role = user?.role;

    // Redirect non-HR users
    useEffect(() => {
        if (role && role !== "SUPER_ADMIN" && role !== "HR_ADMIN") {
            router.replace("/dashboard");
        }
    }, [role]);

    const [letters, setLetters] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [form, setForm] = useState({
        candidateName: "", position: "", department: DEPARTMENTS[0],
        annualCTC: "", joiningDate: "", reportingManager: "",
        workingHours: "9 hours / day, 5 days a week", probationMonths: 6
    });
    const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

    const breakdown = ctcBreakdown(form.annualCTC);

    useEffect(() => {
        api.get("/offer-letters")
            .then(r => setLetters(r.data?.content || []))
            .catch(() => setLetters([]))
            .finally(() => setLoading(false));
    }, []);

    const handleCreate = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const r = await api.post("/offer-letters", {
                ...form,
                annualCTC: Number(form.annualCTC),
                probationMonths: Number(form.probationMonths)
            });
            toast.success(`Offer letter ${r.data.letterNumber} generated! ✅`);
            setLetters(prev => [r.data, ...prev]);
            setShowForm(false);
            setForm({ candidateName: "", position: "", department: DEPARTMENTS[0], annualCTC: "", joiningDate: "", reportingManager: "", workingHours: "9 hours / day, 5 days a week", probationMonths: 6 });
        } catch (err) {
            toast.error(err.response?.data?.error || "Failed to generate offer letter");
        } finally { setSubmitting(false); }
    };

    const download = async (id, name) => {
        try {
            const r = await api.get(`/offer-letters/${id}/download`, { responseType: "blob" });
            const url = URL.createObjectURL(new Blob([r.data], { type: "application/pdf" }));
            const a = document.createElement("a");
            a.href = url;
            a.download = `offer-letter-${name.replace(/\s/g, "-")}.pdf`;
            a.click();
            URL.revokeObjectURL(url);
        } catch { toast.error("Download failed"); }
    };

    return (
        <div>
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Offer Letters</h1>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">Generate and manage candidate offer letters</p>
                </div>
                <button onClick={() => setShowForm(v => !v)}
                    className="inline-flex items-center gap-2 bg-primary-600 text-white px-4 py-2.5 rounded-xl font-medium hover:bg-primary-700 transition shadow-sm text-sm">
                    {showForm ? "✕ Cancel" : "📄 New Offer Letter"}
                </button>
            </div>

            {/* Form */}
            {showForm && (
                <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 p-6 mb-6">
                    <h2 className="font-semibold text-gray-900 dark:text-white mb-5">Generate Offer Letter</h2>
                    <form onSubmit={handleCreate} className="space-y-4">
                        <div className="grid sm:grid-cols-2 gap-4">
                            {[
                                { key: "candidateName", label: "Candidate Name *", type: "text", req: true },
                                { key: "position", label: "Position *", type: "text", req: true },
                                { key: "reportingManager", label: "Reporting Manager", type: "text" },
                                { key: "joiningDate", label: "Joining Date *", type: "date", req: true },
                                { key: "workingHours", label: "Working Hours", type: "text" },
                            ].map(({ key, label, type, req }) => (
                                <div key={key}>
                                    <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">{label}</label>
                                    <input type={type} required={req} value={form[key] || ""}
                                        onChange={e => set(key, e.target.value)}
                                        className="w-full px-3 py-2.5 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                                </div>
                            ))}
                            <div>
                                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Department</label>
                                <select value={form.department} onChange={e => set("department", e.target.value)}
                                    className="w-full px-3 py-2.5 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none">
                                    {DEPARTMENTS.map(d => <option key={d} value={d}>{d}</option>)}
                                </select>
                            </div>
                            <div>
                                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Probation (months)</label>
                                <input type="number" min={1} max={12} value={form.probationMonths}
                                    onChange={e => set("probationMonths", e.target.value)}
                                    className="w-full px-3 py-2.5 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                            </div>
                            <div className="sm:col-span-2">
                                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Annual CTC (₹) *</label>
                                <input type="number" required min={100000} value={form.annualCTC}
                                    onChange={e => set("annualCTC", e.target.value)}
                                    placeholder="e.g. 600000"
                                    className="w-full px-3 py-2.5 border border-gray-200 dark:border-gray-600 rounded-xl text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none" />
                            </div>
                        </div>

                        {/* CTC Preview */}
                        {breakdown && (
                            <div className="bg-gray-50 dark:bg-gray-700/50 rounded-xl p-4 mt-2">
                                <p className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">CTC Breakdown Preview</p>
                                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                                    {[
                                        ["Basic (40%)", breakdown.basic],
                                        ["HRA (20%)", breakdown.hra],
                                        ["Special Allow. (30%)", breakdown.special],
                                        ["Gross Monthly", breakdown.gross / 12],
                                        ["PF Employer (4.81%)", breakdown.pfEmp],
                                        ["Est. Take-Home/mo", breakdown.net],
                                    ].map(([label, val]) => (
                                        <div key={label} className="bg-white dark:bg-gray-800 rounded-lg p-3 text-center">
                                            <p className="text-xs text-gray-400 mb-0.5">{label}</p>
                                            <p className="font-semibold text-sm text-gray-900 dark:text-white">{fmtINR(val)}</p>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        <button type="submit" disabled={submitting}
                            className="bg-primary-600 text-white px-6 py-2.5 rounded-xl font-semibold hover:bg-primary-700 disabled:opacity-50 transition text-sm">
                            {submitting ? "Generating PDF…" : "Generate Offer Letter PDF"}
                        </button>
                    </form>
                </div>
            )}

            {/* List */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 overflow-hidden">
                {loading ? (
                    <div className="p-6 space-y-3">
                        {[1, 2, 3].map(i => <div key={i} className="h-12 bg-gray-100 dark:bg-gray-700 rounded-xl animate-pulse" />)}
                    </div>
                ) : letters.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <p className="text-4xl mb-3">📄</p>
                        <p className="font-medium">No offer letters yet</p>
                        <p className="text-sm mt-1">Click "New Offer Letter" to generate one</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="bg-gray-50 dark:bg-gray-700/50">
                                <tr className="text-left text-gray-500 dark:text-gray-400">
                                    {["Ref #", "Candidate", "Position", "Department", "Annual CTC", "Joining Date", "Download"].map(h => (
                                        <th key={h} className="px-5 py-3.5 font-medium text-xs uppercase tracking-wider">{h}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50 dark:divide-gray-700">
                                {letters.map(ol => (
                                    <tr key={ol.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition">
                                        <td className="px-5 py-4 font-mono text-xs text-gray-500 dark:text-gray-400">{ol.letterNumber || `#${ol.id}`}</td>
                                        <td className="px-5 py-4 font-semibold text-gray-900 dark:text-white">{ol.candidateName}</td>
                                        <td className="px-5 py-4 text-gray-600 dark:text-gray-300">{ol.position}</td>
                                        <td className="px-5 py-4 text-gray-500 dark:text-gray-400">{ol.department || "—"}</td>
                                        <td className="px-5 py-4 text-emerald-700 dark:text-emerald-400 font-semibold">{fmtINR(ol.annualCTC)}</td>
                                        <td className="px-5 py-4 text-gray-500 dark:text-gray-400 text-xs">{ol.joiningDate}</td>
                                        <td className="px-5 py-4">
                                            <button onClick={() => download(ol.id, ol.candidateName)}
                                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg bg-primary-50 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300 hover:bg-primary-100 dark:hover:bg-primary-900/50 transition">
                                                ⬇️ PDF
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}
