"use client";

import { useState } from "react";
import Link from "next/link";

export default function LoginPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081/api";
            const res = await fetch(`${apiUrl}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            const data = await res.json();

            if (!res.ok) throw new Error(data.error || "Invalid credentials");

            localStorage.setItem("token", data.token);
            localStorage.setItem("user", JSON.stringify(data.user));
            document.cookie = `hrms_token=${data.token}; path=/; max-age=${60 * 60 * 24}; SameSite=Strict`;
            window.location.href = "/dashboard";
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
                    <p className="text-gray-500 dark:text-gray-400 mt-2">Sign in to your account</p>
                </div>

                <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-8">
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {error && (
                            <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 px-4 py-3 rounded-xl text-sm">
                                {error}
                            </div>
                        )}

                        <div>
                            <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">Email Address</label>
                            <input id="email" type="email" value={email} autoComplete="email"
                                onChange={(e) => setEmail(e.target.value)} required
                                className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition"
                                placeholder="you@pravarthana.com" />
                        </div>

                        <div>
                            <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">Password</label>
                            <div className="relative">
                                <input id="password" type={showPassword ? "text" : "password"} value={password} autoComplete="current-password"
                                    onChange={(e) => setPassword(e.target.value)} required
                                    className="w-full px-4 py-2.5 pr-12 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition"
                                    placeholder="••••••••" />
                                <button type="button" onClick={() => setShowPassword(!showPassword)} tabIndex={-1}
                                    className="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition"
                                    aria-label={showPassword ? "Hide password" : "Show password"}>
                                    {showPassword
                                        ? <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.477 0-8.268-2.943-9.542-7a9.956 9.956 0 012.22-3.592M6.53 6.53A9.956 9.956 0 0112 5c4.477 0 8.268 2.943 9.542 7a9.972 9.972 0 01-4.07 5.304M15 12a3 3 0 11-6 0 3 3 0 016 0zM3 3l18 18" /></svg>
                                        : <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                                    }
                                </button>
                            </div>
                        </div>

                        <button type="submit" disabled={loading}
                            className="w-full bg-primary-600 text-white py-2.5 px-4 rounded-xl font-semibold hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition shadow-md">
                            {loading ? "Signing in…" : "Sign In"}
                        </button>
                    </form>

                    <div className="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800 rounded-xl text-center space-y-2">
                        <p className="text-sm text-blue-700 dark:text-blue-300">🔒 Contact your administrator for login credentials.</p>
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                            New to the system?{" "}
                            <Link href="/register" className="text-primary-600 hover:underline font-medium">Register here</Link>
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
