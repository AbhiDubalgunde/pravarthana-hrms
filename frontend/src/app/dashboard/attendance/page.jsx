"use client";

import { useState, useEffect } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import api from "@/services/api";
import { useAuth } from "@/context/AuthContext";

const STATUS_CONFIG = {
    PRESENT: { color: "#16a34a", bg: "#dcfce7", label: "Present" },
    ABSENT: { color: "#dc2626", bg: "#fee2e2", label: "Absent" },
    LEAVE: { color: "#d97706", bg: "#fef3c7", label: "Leave" },
    HALF_DAY: { color: "#2563eb", bg: "#dbeafe", label: "Half Day" },
};

export default function AttendancePage() {
    const { user } = useAuth();
    const isAdmin = user?.role === "SUPER_ADMIN" || user?.role === "HR_ADMIN";
    const isTeamLead = user?.role === "TEAM_LEAD";

    const [events, setEvents] = useState([]);
    const [summary, setSummary] = useState({ PRESENT: 0, ABSENT: 0, LEAVE: 0, HALF_DAY: 0 });
    const [loading, setLoading] = useState(false);
    const [myEmployee, setMyEmployee] = useState(null);
    const [employees, setEmployees] = useState([]);
    const [selectedEmp, setSelectedEmp] = useState(null);
    const [currentMonth, setCurrentMonth] = useState(() => {
        const now = new Date();
        return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
    });
    const [punchState, setPunchState] = useState({ status: null, checkIn: null, checkOut: null });

    // Load my employee profile
    useEffect(() => {
        api.get("/employees/me").then((r) => {
            setMyEmployee(r.data);
            setSelectedEmp(r.data.id);
        }).catch(() => { });
    }, []);

    // Load employees list (HR/Admin and Team Lead)
    useEffect(() => {
        if (isAdmin) {
            api.get("/employees?size=200").then((r) =>
                setEmployees(r.data.content || [])
            ).catch(() => { });
        } else if (isTeamLead && myEmployee) {
            api.get(`/employees/team/${myEmployee.id}`).then((r) =>
                setEmployees([myEmployee, ...(r.data || [])])
            ).catch(() => setEmployees(myEmployee ? [myEmployee] : []));
        }
    }, [isAdmin, isTeamLead, myEmployee]);

    // Load today's status for punch card
    useEffect(() => {
        if (!myEmployee) return;
        api.get(`/attendance/today/${myEmployee.id}`).then((r) => {
            setPunchState({
                status: r.data.status,
                checkIn: r.data.checkInTime,
                checkOut: r.data.checkOutTime,
            });
        }).catch(() => { });
    }, [myEmployee]);

    // Load calendar data
    useEffect(() => {
        const empId = selectedEmp;
        if (!empId) return;
        setLoading(true);
        api.get(`/attendance/monthly/${empId}?month=${currentMonth}`)
            .then((r) => {
                const records = r.data || [];
                const calEvents = records.map((rec) => {
                    const cfg = STATUS_CONFIG[rec.status] || STATUS_CONFIG.ABSENT;
                    return {
                        title: cfg.label,
                        date: rec.date,
                        backgroundColor: cfg.color,
                        borderColor: cfg.color,
                        textColor: "#fff",
                        extendedProps: rec,
                    };
                });
                setEvents(calEvents);
                const counts = { PRESENT: 0, ABSENT: 0, LEAVE: 0, HALF_DAY: 0 };
                records.forEach((r) => { if (counts[r.status] !== undefined) counts[r.status]++; });
                setSummary(counts);
            })
            .catch(() => { setEvents([]); setSummary({ PRESENT: 0, ABSENT: 0, LEAVE: 0, HALF_DAY: 0 }); })
            .finally(() => setLoading(false));
    }, [selectedEmp, currentMonth]);

    const handlePunch = async (type) => {
        try {
            await api.post(`/attendance/${type}`);
            const r = await api.get(`/attendance/today/${myEmployee.id}`);
            setPunchState({ status: r.data.status, checkIn: r.data.checkInTime, checkOut: r.data.checkOutTime });
        } catch (e) {
            alert(e?.response?.data?.message || `Punch ${type} failed`);
        }
    };

    const selectedEmployee = (isAdmin || isTeamLead)
        ? employees.find((e) => e.id === selectedEmp)
        : myEmployee;

    return (
        <div className="max-w-6xl mx-auto space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Attendance Calendar</h1>
                    {selectedEmployee && (
                        <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
                            {selectedEmployee.firstName} {selectedEmployee.lastName} · {selectedEmployee.department || "—"}
                        </p>
                    )}
                </div>
                {/* Employee Selector (HR/Admin & Team Lead) */}
                {(isAdmin || isTeamLead) && employees.length > 0 && (
                    <select
                        value={selectedEmp || ""}
                        onChange={(e) => setSelectedEmp(Number(e.target.value))}
                        className="px-3 py-2 text-sm bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500"
                    >
                        {employees.map((emp) => (
                            <option key={emp.id} value={emp.id}>
                                {emp.firstName} {emp.lastName}
                            </option>
                        ))}
                    </select>
                )}
            </div>

            {/* Punch Card (only for own profile) */}
            {myEmployee && selectedEmp === myEmployee.id && (
                <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-100 dark:border-gray-800 p-4 flex flex-wrap items-center gap-4">
                    <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-700 dark:text-gray-300">Today's Status</p>
                        <p className={`text-lg font-bold ${punchState.status === "PRESENT" ? "text-green-600" :
                                punchState.status === "HALF_DAY" ? "text-blue-600" :
                                    "text-gray-400"
                            }`}>
                            {punchState.status || "Not recorded"}
                        </p>
                        {punchState.checkIn && (
                            <p className="text-xs text-gray-500 mt-0.5">
                                In: {punchState.checkIn}
                                {punchState.checkOut && ` · Out: ${punchState.checkOut}`}
                            </p>
                        )}
                    </div>
                    <div className="flex gap-2">
                        <button
                            onClick={() => handlePunch("punch-in")}
                            disabled={!!punchState.checkIn}
                            className="px-4 py-2 text-sm font-medium bg-green-500 text-white rounded-xl hover:bg-green-600 disabled:opacity-40 disabled:cursor-not-allowed transition"
                        >
                            Punch In
                        </button>
                        <button
                            onClick={() => handlePunch("punch-out")}
                            disabled={!punchState.checkIn || !!punchState.checkOut}
                            className="px-4 py-2 text-sm font-medium bg-red-500 text-white rounded-xl hover:bg-red-600 disabled:opacity-40 disabled:cursor-not-allowed transition"
                        >
                            Punch Out
                        </button>
                    </div>
                </div>
            )}

            {/* Summary Pills */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                {Object.entries(STATUS_CONFIG).map(([status, cfg]) => (
                    <div key={status} className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-100 dark:border-gray-800 p-4 flex items-center gap-3">
                        <div className="w-3 h-3 rounded-full flex-shrink-0" style={{ backgroundColor: cfg.color }} />
                        <div>
                            <p className="text-2xl font-bold text-gray-900 dark:text-white">{summary[status]}</p>
                            <p className="text-xs text-gray-500">{cfg.label}</p>
                        </div>
                    </div>
                ))}
            </div>

            {/* Calendar */}
            <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-100 dark:border-gray-800 p-4 shadow-sm">
                {loading ? (
                    <div className="flex items-center justify-center py-16">
                        <div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" />
                    </div>
                ) : (
                    <FullCalendar
                        plugins={[dayGridPlugin]}
                        initialView="dayGridMonth"
                        events={events}
                        height="auto"
                        datesSet={(info) => {
                            const d = info.view.currentStart;
                            const ym = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
                            setCurrentMonth(ym);
                        }}
                        eventContent={(arg) => (
                            <div className="px-1.5 py-0.5 text-xs font-medium rounded truncate w-full">
                                {arg.event.title}
                            </div>
                        )}
                        headerToolbar={{
                            left: "prev,next today",
                            center: "title",
                            right: ""
                        }}
                    />
                )}
            </div>

            {/* Legend */}
            <div className="flex flex-wrap gap-4 text-xs px-1">
                {Object.entries(STATUS_CONFIG).map(([status, cfg]) => (
                    <div key={status} className="flex items-center gap-1.5">
                        <div className="w-3 h-3 rounded-sm" style={{ backgroundColor: cfg.color }} />
                        <span className="text-gray-500 dark:text-gray-400">{cfg.label}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}
