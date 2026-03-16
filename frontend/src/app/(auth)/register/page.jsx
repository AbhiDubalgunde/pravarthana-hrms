"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

const ROLES = [
    { value: "EMPLOYEE", label: "Employee" },
    { value: "TEAM_LEAD", label: "Team Lead" },
    { value: "HR_ADMIN", label: "HR Admin" },
    { value: "SUPER_ADMIN", label: "Super Admin" },
];

function EyeIcon({ open }) {
    if (open) {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.477 0-8.268-2.943-9.542-7a9.956 9.956 0 012.22-3.592M6.53 6.53A9.956 9.956 0 0112 5c4.477 0 8.268 2.943 9.542 7a9.972 9.972 0 01-4.07 5.304M15 12a3 3 0 11-6 0 3 3 0 016 0zM3 3l18 18" />
            </svg>
        );
    }
    return (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
        </svg>
    );
}

export default function RegisterPage() {
    const router = useRouter();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [role, setRole] = useState("EMPLOYEE");
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");

        if (password !== confirmPassword) { setError("Passwords do not match."); return; }
        if (password.length < 6) { setError("Password must be at least 6 characters."); return; }

        setLoading(true);
        try {
            const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081/api";
            const res = await fetch(`${apiUrl}/auth/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password, role }),
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.error || "Registration failed");
            setSuccess("Account created! Redirecting to login…");
            setTimeout(() => router.push("/login"), 1800);
        } catch (err) {
            setError(err.message || "Something went wrong. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-primary-50 to-gray-100 dark:from-gray-900 dark:to-gray-950 flex items-center justify-center px-4">
            <div className="max-w-md w-full">
                <div className="text-center mb-8">
                    <Link href="/" className="text-3xl font-extrabold text-primary-600">Pravarthana HRMS</Link>
                    <p className="text-gray-500 dark:text-gray-400 mt-2">Create your account</p>
                </div>

                <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-8">
                    <form onSubmit={handleSubmit} className="space-y-5">
                        {error && <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 px-4 py-3 rounded-xl text-sm">{error}</div>}
                        {success && <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 text-green-700 dark:text-green-300 px-4 py-3 rounded-xl text-sm">✅ {success}</div>}

                        <div>
                            <label htmlFor="reg-email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">Email Address</label>
                            <input id="reg-email" type="email" value={email} autoComplete="email" onChange={(e) => setEmail(e.target.value)} required
                                className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none transition"
                                placeholder="you@pravarthana.com" />
                        </div>

                        <div>
                            <label htmlFor="reg-role" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">Role</label>
                            <select id="reg-role" value={role} onChange={(e) => setRole(e.target.value)}
                                className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none transition">
                                {ROLES.map((r) => <option key={r.value} value={r.value}>{r.label}</option>)}
                            </select>
                        </div>

                        {[
                            { id: "reg-password", label: "Password", val: password, setVal: setPassword, show: showPassword, setShow: setShowPassword, ph: "Min. 6 characters" },
                            { id: "reg-confirm", label: "Confirm Password", val: confirmPassword, setVal: setConfirmPassword, show: showConfirm, setShow: setShowConfirm, ph: "Re-enter password" },
                        ].map(({ id, label, val, setVal, show, setShow, ph }) => (
                            <div key={id}>
                                <label htmlFor={id} className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">{label}</label>
                                <div className="relative">
                                    <input id={id} type={show ? "text" : "password"} value={val} autoComplete="new-password" onChange={(e) => setVal(e.target.value)} required
                                        className="w-full px-4 py-2.5 pr-12 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none transition"
                                        placeholder={ph} />
                                    <button type="button" onClick={() => setShow(!show)} tabIndex={-1}
                                        className="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition">
                                        <EyeIcon open={show} />
                                    </button>
                                </div>
                            </div>
                        ))}

                        <button type="submit" disabled={loading}
                            className="w-full bg-primary-600 text-white py-2.5 px-4 rounded-xl font-semibold hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition shadow-md mt-2">
                            {loading ? "Creating account…" : "Create Account"}
                        </button>
                    </form>

                    <div className="mt-6 text-center">
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                            Already have an account?{" "}
                            <Link href="/login" className="text-primary-600 hover:underline font-medium">Sign in here</Link>
                        </p>
                    </div>
                </div>

                <div className="text-center mt-6">
                    <Link href="/" className="text-sm text-gray-500 hover:text-gray-800 dark:hover:text-gray-200">← Back to Home</Link>
                </div>
            </div>
        </div>
    );
}
