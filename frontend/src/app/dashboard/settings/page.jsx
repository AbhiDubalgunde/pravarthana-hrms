"use client";

import { useState, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
import api from "@/services/api";

const SECTIONS = ["Profile", "Security", "Notifications", "Account"];

export default function SettingsPage() {
    const { user, logout } = useAuth();
    const [activeTab, setActiveTab] = useState("Profile");
    const [profile, setProfile] = useState({ designation: "", phone: "" });
    const [passwords, setPasswords] = useState({ current: "", next: "", confirm: "" });
    const [notifs, setNotifs] = useState({ leaveAlerts: true, payrollAlerts: true, systemAlerts: true });
    const [saving, setSaving] = useState(false);
    const [msg, setMsg] = useState(null); // { type: "success"|"error", text }
    const [myEmployee, setMyEmployee] = useState(null);

    useEffect(() => {
        api.get("/employees/me")
            .then(r => {
                setMyEmployee(r.data);
                setProfile({ designation: r.data.designation || "", phone: r.data.phone || "" });
            })
            .catch(() => { });
    }, []);

    const flash = (type, text) => {
        setMsg({ type, text });
        setTimeout(() => setMsg(null), 4000);
    };

    const saveProfile = async () => {
        setSaving(true);
        try {
            if (myEmployee?.id) {
                await api.put(`/employees/${myEmployee.id}`, profile);
                flash("success", "Profile updated successfully!");
            }
        } catch {
            flash("error", "Failed to update profile.");
        } finally { setSaving(false); }
    };

    const changePassword = async () => {
        if (passwords.next !== passwords.confirm) {
            flash("error", "New passwords do not match.");
            return;
        }
        if (passwords.next.length < 8) {
            flash("error", "Password must be at least 8 characters.");
            return;
        }
        setSaving(true);
        try {
            await api.put("/auth/change-password", {
                currentPassword: passwords.current,
                newPassword: passwords.next,
            });
            flash("success", "Password changed successfully!");
            setPasswords({ current: "", next: "", confirm: "" });
        } catch (e) {
            flash("error", e?.response?.data?.error || "Failed to change password.");
        } finally { setSaving(false); }
    };

    const tabClass = (t) =>
        `px-4 py-2.5 text-sm font-medium rounded-xl transition-all ${activeTab === t
            ? "bg-primary-600 text-white shadow"
            : "text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800"
        }`;

    const inputClass =
        "w-full px-4 py-2.5 bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 " +
        "rounded-xl text-sm text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none " +
        "focus:ring-2 focus:ring-primary-500 transition";

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            {/* Header */}
            <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Settings</h1>
                <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                    Manage your account, security, and preferences
                </p>
            </div>

            {/* Flash message */}
            {msg && (
                <div className={`px-4 py-3 rounded-xl text-sm font-medium border ${msg.type === "success"
                        ? "bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800 text-green-700 dark:text-green-300"
                        : "bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800 text-red-700 dark:text-red-300"
                    }`}>
                    {msg.text}
                </div>
            )}

            {/* Tabs */}
            <div className="flex gap-2 flex-wrap">
                {SECTIONS.map(s => (
                    <button key={s} onClick={() => setActiveTab(s)} className={tabClass(s)}>{s}</button>
                ))}
            </div>

            {/* ── Profile Tab ─────────────────────────────────────────────── */}
            {activeTab === "Profile" && (
                <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-100 dark:border-gray-800 shadow-sm p-6 space-y-5">
                    <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Profile Information</h2>

                    {/* Avatar */}
                    <div className="flex items-center gap-4">
                        <div className="w-16 h-16 rounded-2xl bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-2xl font-bold text-primary-600 dark:text-primary-300">
                            {(user?.fullName || user?.email || "?")[0].toUpperCase()}
                        </div>
                        <div>
                            <p className="font-semibold text-gray-900 dark:text-white">{user?.fullName || user?.email}</p>
                            <span className="inline-block mt-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary-100 dark:bg-primary-900/40 text-primary-700 dark:text-primary-300">
                                {user?.role?.replace("_", " ")}
                            </span>
                        </div>
                    </div>

                    {/* Read-only info */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5">Email</label>
                            <div className={`${inputClass} bg-gray-100 dark:bg-gray-700/50 cursor-not-allowed text-gray-500`}>
                                {user?.email || "—"}
                            </div>
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5">Employee Code</label>
                            <div className={`${inputClass} bg-gray-100 dark:bg-gray-700/50 cursor-not-allowed text-gray-500`}>
                                {myEmployee?.employeeCode || "—"}
                            </div>
                        </div>
                    </div>

                    {/* Editable */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5">Designation</label>
                            <input
                                className={inputClass}
                                value={profile.designation}
                                onChange={e => setProfile(p => ({ ...p, designation: e.target.value }))}
                                placeholder="e.g. Software Engineer"
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5">Phone</label>
                            <input
                                className={inputClass}
                                value={profile.phone}
                                onChange={e => setProfile(p => ({ ...p, phone: e.target.value }))}
                                placeholder="+91 XXXXX XXXXX"
                            />
                        </div>
                    </div>

                    <button
                        onClick={saveProfile}
                        disabled={saving || !myEmployee}
                        className="px-6 py-2.5 bg-primary-600 text-white text-sm font-medium rounded-xl hover:bg-primary-700 disabled:opacity-50 transition"
                    >
                        {saving ? "Saving…" : "Save Changes"}
                    </button>
                </div>
            )}

            {/* ── Security Tab ────────────────────────────────────────────── */}
            {activeTab === "Security" && (
                <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-100 dark:border-gray-800 shadow-sm p-6 space-y-5">
                    <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Change Password</h2>
                    {[
                        { key: "current", label: "Current Password" },
                        { key: "next", label: "New Password" },
                        { key: "confirm", label: "Confirm New Password" },
                    ].map(({ key, label }) => (
                        <div key={key}>
                            <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1.5">{label}</label>
                            <input
                                type="password"
                                className={inputClass}
                                value={passwords[key]}
                                onChange={e => setPasswords(p => ({ ...p, [key]: e.target.value }))}
                                placeholder="••••••••"
                            />
                        </div>
                    ))}
                    <button
                        onClick={changePassword}
                        disabled={saving || !passwords.current || !passwords.next}
                        className="px-6 py-2.5 bg-primary-600 text-white text-sm font-medium rounded-xl hover:bg-primary-700 disabled:opacity-50 transition"
                    >
                        {saving ? "Updating…" : "Update Password"}
                    </button>
                </div>
            )}

            {/* ── Notifications Tab ────────────────────────────────────────── */}
            {activeTab === "Notifications" && (
                <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-100 dark:border-gray-800 shadow-sm p-6 space-y-4">
                    <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Notification Preferences</h2>
                    {[
                        { key: "leaveAlerts", label: "Leave approval notifications" },
                        { key: "payrollAlerts", label: "Payroll and salary updates" },
                        { key: "systemAlerts", label: "System announcements" },
                    ].map(({ key, label }) => (
                        <label key={key} className="flex items-center justify-between gap-4 cursor-pointer group">
                            <span className="text-sm text-gray-700 dark:text-gray-300">{label}</span>
                            <div
                                onClick={() => setNotifs(n => ({ ...n, [key]: !n[key] }))}
                                className={`relative w-11 h-6 rounded-full transition-colors cursor-pointer flex-shrink-0 ${notifs[key] ? "bg-primary-600" : "bg-gray-200 dark:bg-gray-700"
                                    }`}
                            >
                                <div className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform ${notifs[key] ? "translate-x-5" : ""
                                    }`} />
                            </div>
                        </label>
                    ))}
                    <p className="text-xs text-gray-400 mt-2">Notification preferences are stored locally.</p>
                </div>
            )}

            {/* ── Account Tab ─────────────────────────────────────────────── */}
            {activeTab === "Account" && (
                <div className="space-y-4">
                    <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-100 dark:border-gray-800 shadow-sm p-6">
                        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Account Details</h2>
                        <div className="grid grid-cols-2 gap-3 text-sm">
                            {[
                                ["Role", user?.role?.replace("_", " ")],
                                ["Department", myEmployee?.departmentName || "Engineering"],
                                ["Joined", myEmployee?.joiningDate ? new Date(myEmployee.joiningDate).toLocaleDateString("en-IN") : "—"],
                                ["Status", myEmployee?.status || "ACTIVE"],
                            ].map(([k, v]) => (
                                <div key={k} className="p-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                                    <p className="text-xs text-gray-500 dark:text-gray-400">{k}</p>
                                    <p className="font-semibold text-gray-900 dark:text-white mt-0.5">{v || "—"}</p>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="bg-red-50 dark:bg-red-900/10 rounded-2xl border border-red-100 dark:border-red-800/40 p-6">
                        <h3 className="font-semibold text-red-700 dark:text-red-400 mb-2">Danger Zone</h3>
                        <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">Sign out from all sessions.</p>
                        <button
                            onClick={logout}
                            className="px-5 py-2 bg-red-600 text-white text-sm font-medium rounded-xl hover:bg-red-700 transition"
                        >
                            Sign Out
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
