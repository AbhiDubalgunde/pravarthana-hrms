"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { employeeService, attendanceService } from "@/services/api";

const STATUS_COLORS = {
    PRESENT: "bg-emerald-500",
    ABSENT: "bg-red-400",
    HALF_DAY: "bg-amber-400",
    LEAVE: "bg-blue-400",
};
const STATUS_LABELS = {
    PRESENT: "Present", ABSENT: "Absent", HALF_DAY: "Half Day", LEAVE: "Leave",
};

function AttendanceCalendar({ employeeId }) {
    const [year, setYear] = useState(new Date().getFullYear());
    const [month, setMonth] = useState(new Date().getMonth() + 1);
    const [records, setRecords] = useState([]);
    const [selected, setSelected] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function load() {
            setLoading(true);
            try {
                const monthStr = `${year}-${String(month).padStart(2, "0")}`;
                const res = await attendanceService.getMonthly(employeeId, monthStr);
                setRecords(res.data || []);
            } catch (_) { setRecords([]); }
            finally { setLoading(false); }
        }
        if (employeeId) load();
    }, [employeeId, year, month]);

    const recordMap = records.reduce((acc, r) => {
        acc[r.date] = r;
        return acc;
    }, {});

    const daysInMonth = new Date(year, month, 0).getDate();
    const firstDayOfWeek = new Date(year, month - 1, 1).getDay(); // 0=Sun
    const monthStr = `${year}-${String(month).padStart(2, "0")}`;
    const monthName = new Date(year, month - 1, 1).toLocaleDateString("en-IN", { month: "long", year: "numeric" });

    const prevMonth = () => { if (month === 1) { setYear(y => y - 1); setMonth(12); } else setMonth(m => m - 1); };
    const nextMonth = () => { if (month === 12) { setYear(y => y + 1); setMonth(1); } else setMonth(m => m + 1); };

    return (
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 p-5">
            <div className="flex items-center justify-between mb-4">
                <h2 className="font-semibold text-gray-900 dark:text-white">📅 Attendance Calendar</h2>
                <div className="flex items-center gap-2">
                    <button onClick={prevMonth} className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-500 transition">‹</button>
                    <span className="text-sm font-medium text-gray-700 dark:text-gray-200 min-w-[140px] text-center">{monthName}</span>
                    <button onClick={nextMonth} className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-500 transition">›</button>
                </div>
            </div>

            {loading ? (
                <div className="h-48 bg-gray-100 dark:bg-gray-700 rounded-xl animate-pulse" />
            ) : (
                <>
                    <div className="grid grid-cols-7 gap-1 mb-1">
                        {["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"].map(d => (
                            <div key={d} className="text-center text-xs font-medium text-gray-400 py-1">{d}</div>
                        ))}
                    </div>
                    <div className="grid grid-cols-7 gap-1">
                        {Array.from({ length: firstDayOfWeek }).map((_, i) => <div key={`e${i}`} />)}
                        {Array.from({ length: daysInMonth }).map((_, i) => {
                            const day = i + 1;
                            const date = `${monthStr}-${String(day).padStart(2, "0")}`;
                            const rec = recordMap[date];
                            const isToday = date === new Date().toISOString().split("T")[0];
                            return (
                                <button key={day} onClick={() => rec ? setSelected(rec) : null}
                                    className={`relative aspect-square rounded-xl flex flex-col items-center justify-center text-xs
                    ${rec ? "cursor-pointer hover:ring-2 hover:ring-primary-400" : "cursor-default"}
                    ${isToday ? "ring-2 ring-primary-500" : ""}
                    ${rec ? `${STATUS_COLORS[rec.status]} text-white` : "bg-gray-50 dark:bg-gray-700 text-gray-500 dark:text-gray-400"}
                    transition`}>
                                    <span className="font-semibold">{day}</span>
                                    {rec && <span className="text-[9px] opacity-80">{rec.status === "HALF_DAY" ? "Half" : ""}</span>}
                                </button>
                            );
                        })}
                    </div>
                </>
            )}

            {/* Legend */}
            <div className="flex flex-wrap gap-3 mt-4">
                {Object.entries(STATUS_LABELS).map(([k, v]) => (
                    <div key={k} className="flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                        <div className={`w-3 h-3 rounded-full ${STATUS_COLORS[k]}`} />
                        {v}
                    </div>
                ))}
            </div>

            {/* Day Detail Modal */}
            {selected && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-black/50" onClick={() => setSelected(null)} />
                    <div className="relative bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-6 min-w-[280px] z-10">
                        <h3 className="font-bold text-gray-900 dark:text-white mb-4">{selected.date}</h3>
                        <div className="space-y-2">
                            {[
                                ["Status", STATUS_LABELS[selected.status] || selected.status],
                                ["Check In", selected.checkInTime || "—"],
                                ["Check Out", selected.checkOutTime || "—"],
                                ["Total Hours", selected.totalHours != null ? `${selected.totalHours}h` : "—"],
                            ].map(([l, v]) => (
                                <div key={l} className="flex justify-between">
                                    <span className="text-sm text-gray-500">{l}</span>
                                    <span className="text-sm font-semibold text-gray-900 dark:text-white">{v}</span>
                                </div>
                            ))}
                        </div>
                        <button onClick={() => setSelected(null)}
                            className="mt-5 w-full py-2 rounded-xl border border-gray-200 dark:border-gray-600 text-sm text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition">
                            Close
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default function EmployeeProfilePage() {
    const { id } = useParams();
    const [emp, setEmp] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function load() {
            try {
                const res = await employeeService.getById(id);
                setEmp(res.data);
            } catch (err) {
                setError(err.response?.data?.error || "Employee not found");
            } finally { setLoading(false); }
        }
        if (id) load();
    }, [id]);

    if (loading) {
        return (
            <div className="space-y-4">
                <div className="h-40 bg-gray-100 dark:bg-gray-800 rounded-2xl animate-pulse" />
                <div className="h-64 bg-gray-100 dark:bg-gray-800 rounded-2xl animate-pulse" />
            </div>
        );
    }
    if (error) {
        return (
            <div className="text-center py-20">
                <p className="text-5xl mb-3">❌</p>
                <p className="text-lg font-semibold text-gray-900 dark:text-white">{error}</p>
                <a href="/dashboard/employees" className="mt-4 inline-block text-primary-600 hover:underline">← Back to Employees</a>
            </div>
        );
    }

    const details = [
        ["Employee Code", emp.employeeCode],
        ["Email", emp.email],
        ["Phone", emp.phone || "—"],
        ["Department", emp.department || "—"],
        ["Designation", emp.designation || "—"],
        ["Joining Date", emp.joiningDate || "—"],
        ["Status", emp.status],
    ];

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
                <a href="/dashboard/employees" className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
                </a>
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                        {emp.fullName || `${emp.firstName} ${emp.lastName || ""}`.trim()}
                    </h1>
                    <p className="text-sm text-gray-500 dark:text-gray-400">{emp.designation} · {emp.department}</p>
                </div>
            </div>

            {/* Profile Card */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 p-6">
                <div className="flex items-start gap-5 mb-6">
                    <div className="w-16 h-16 rounded-2xl bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-primary-700 dark:text-primary-300 font-extrabold text-2xl flex-shrink-0">
                        {(emp.firstName || "?")[0].toUpperCase()}
                    </div>
                    <div>
                        <h2 className="text-xl font-bold text-gray-900 dark:text-white">
                            {emp.fullName || `${emp.firstName} ${emp.lastName || ""}`.trim()}
                        </h2>
                        <p className="text-gray-500 dark:text-gray-400">{emp.email}</p>
                        <span className={`inline-block mt-2 px-3 py-1 rounded-full text-xs font-medium ${emp.status === "ACTIVE"
                                ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400"
                                : "bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400"
                            }`}>{emp.status}</span>
                    </div>
                </div>

                <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    {details.map(([label, value]) => (
                        <div key={label}>
                            <p className="text-xs text-gray-400 mb-0.5">{label}</p>
                            <p className="text-sm font-medium text-gray-900 dark:text-white">{value}</p>
                        </div>
                    ))}
                </div>
            </div>

            {/* Attendance Calendar */}
            <AttendanceCalendar employeeId={emp.id} />
        </div>
    );
}
