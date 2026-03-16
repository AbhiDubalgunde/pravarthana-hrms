"use client";

import { useEffect, useState } from "react";
import api from "@/services/api";
import { useAuth } from "@/context/AuthContext";

export default function DashboardPage() {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [today, setToday] = useState(null);
    const [loading, setLoading] = useState(true);

    const fullName = user?.fullName || user?.email || "User";
    const firstName = fullName.split(" ")[0];
    const role = user?.role || "EMPLOYEE";
    const isAdmin = ["SUPER_ADMIN", "HR_ADMIN"].includes(role);

    useEffect(() => {
        async function load() {
            const calls = [
                api.get("/dashboard/stats").catch(() => null),
                api.get("/employees/me").then(r =>
                    api.get(`/attendance/today/${r.data.id}`).catch(() => null)
                ).catch(() => null),
            ];
            const [statsRes, todayRes] = await Promise.allSettled(calls);
            if (statsRes.status === "fulfilled" && statsRes.value) setStats(statsRes.value.data);
            if (todayRes.status === "fulfilled" && todayRes.value) setToday(todayRes.value.data);
            setLoading(false);
        }
        load();
    }, []);

    const now = new Date();
    const greeting = now.getHours() < 12 ? "Good morning" : now.getHours() < 17 ? "Good afternoon" : "Good evening";
    const hasPunchedIn = !!today?.checkInTime;
    const hasPunchedOut = !!today?.checkOutTime;

    const StatCard = ({ label, value, icon, colorCls }) => (
        <div className={`rounded-2xl p-5 border ${colorCls}`}>
            <div className="flex items-center justify-between mb-3">
                <span className="text-2xl">{icon}</span>
            </div>
            <p className="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wide">{label}</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-white mt-0.5">
                {loading ? <span className="inline-block w-10 h-6 bg-gray-200 dark:bg-gray-700 rounded animate-pulse" /> : (value ?? "—")}
            </p>
        </div>
    );

    const quickActions = [
        { title: "Attendance", desc: "Check-in & monthly history", icon: "📅", href: "/dashboard/attendance", color: "from-teal-500 to-teal-600" },
        { title: "Apply Leave", desc: "Request time off", icon: "📝", href: "/dashboard/leave", color: "from-purple-500 to-purple-600" },
        { title: "Employees", desc: "View & manage team", icon: "👥", href: "/dashboard/employees", color: "from-blue-500 to-blue-600" },
    ];

    return (
        <div className="space-y-6">
            {/* Welcome */}
            <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                    {greeting}, {firstName}! 👋
                </h1>
                <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                    {now.toLocaleDateString("en-IN", { weekday: "long", year: "numeric", month: "long", day: "numeric" })}
                </p>
            </div>

            {/* Admin stats row */}
            {isAdmin && (
                <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                    <StatCard label="Total Employees" value={stats?.totalEmployees} icon="👥" colorCls="bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800" />
                    <StatCard label="Present Today" value={stats?.presentToday} icon="✅" colorCls="bg-emerald-50 dark:bg-emerald-900/20 border-emerald-200 dark:border-emerald-800" />
                    <StatCard label="On Leave" value={stats?.onLeave} icon="🏖️" colorCls="bg-amber-50 dark:bg-amber-900/20 border-amber-200 dark:border-amber-800" />
                    <StatCard label="Pending Leaves" value={stats?.pendingLeaves} icon="⏳" colorCls="bg-purple-50 dark:bg-purple-900/20 border-purple-200 dark:border-purple-800" />
                </div>
            )}

            {/* Today's attendance status */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 p-6">
                <h2 className="font-semibold text-gray-900 dark:text-white mb-4">⏱ Today's Status</h2>
                {loading ? (
                    <div className="h-24 bg-gray-100 dark:bg-gray-700 rounded-xl animate-pulse" />
                ) : (
                    <div className={`rounded-xl p-4 text-center ${hasPunchedOut ? "bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-200 dark:border-emerald-800"
                            : hasPunchedIn ? "bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800"
                                : "bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600"}`}>
                        <p className="text-3xl mb-1">
                            {hasPunchedOut ? "✅" : hasPunchedIn ? "🟢" : "⭕"}
                        </p>
                        <p className={`font-bold ${hasPunchedOut ? "text-emerald-700 dark:text-emerald-300"
                            : hasPunchedIn ? "text-blue-700 dark:text-blue-300" : "text-gray-500 dark:text-gray-400"}`}>
                            {hasPunchedOut ? "Work Complete" : hasPunchedIn ? "Currently Working" : "Not Punched In"}
                        </p>
                        {today && (
                            <div className="flex justify-center gap-6 mt-2 text-sm text-gray-500 dark:text-gray-400">
                                {today.checkInTime && <span>In: <b className="text-gray-900 dark:text-white">{today.checkInTime}</b></span>}
                                {today.checkOutTime && <span>Out: <b className="text-gray-900 dark:text-white">{today.checkOutTime}</b></span>}
                                {today.totalHours != null && <span>Hours: <b className="text-gray-900 dark:text-white">{today.totalHours}h</b></span>}
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Quick Actions */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 p-6">
                <h2 className="font-semibold text-gray-900 dark:text-white mb-4">Quick Actions</h2>
                <div className="grid sm:grid-cols-3 gap-4">
                    {quickActions.map(action => (
                        <a key={action.title} href={action.href}
                            className={`group bg-gradient-to-br ${action.color} rounded-xl p-5 text-left transition-all hover:scale-[1.02] hover:shadow-lg relative overflow-hidden`}>
                            <div className="relative z-10">
                                <div className="text-3xl mb-2">{action.icon}</div>
                                <h4 className="text-white font-bold text-base mb-0.5">{action.title}</h4>
                                <p className="text-white/80 text-sm">{action.desc}</p>
                            </div>
                            <div className="absolute top-0 right-0 w-20 h-20 bg-white/10 rounded-full -mr-10 -mt-10 group-hover:scale-150 transition-transform" />
                        </a>
                    ))}
                </div>
            </div>
        </div>
    );
}
