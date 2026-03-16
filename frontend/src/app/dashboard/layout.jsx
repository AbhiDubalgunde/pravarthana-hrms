"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { AuthProvider, useAuth } from "@/context/AuthContext";
import Sidebar from "@/components/Sidebar";
import ThemeToggle from "@/components/ThemeToggle";

function DashboardShell({ children }) {
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const { user, ready, logout } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (ready && !user) router.replace("/login");
    }, [ready, user, router]);

    if (!ready) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950">
                <div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" />
            </div>
        );
    }
    if (!user) return null;

    return (
        <div className="flex h-screen bg-gray-50 dark:bg-gray-950 overflow-hidden">
            {/* Desktop Sidebar */}
            <div className="hidden md:flex flex-shrink-0">
                <Sidebar />
            </div>

            {/* Mobile Sidebar Overlay */}
            {sidebarOpen && (
                <div className="fixed inset-0 z-40 md:hidden">
                    <div className="absolute inset-0 bg-black/50" onClick={() => setSidebarOpen(false)} />
                    <div className="relative z-50 w-64 h-full">
                        <Sidebar onClose={() => setSidebarOpen(false)} />
                    </div>
                </div>
            )}

            {/* Main Area */}
            <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
                {/* Topbar */}
                <header className="h-14 flex items-center justify-between px-4 sm:px-6 bg-white dark:bg-gray-900 border-b border-gray-100 dark:border-gray-800 flex-shrink-0">
                    <button onClick={() => setSidebarOpen(true)}
                        className="md:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                        </svg>
                    </button>
                    <div className="flex-1 md:flex-none" />
                    <div className="flex items-center gap-2">
                        <ThemeToggle />
                        <div className="flex items-center gap-2 pl-2 border-l border-gray-100 dark:border-gray-800">
                            <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center text-primary-700 dark:text-primary-300 font-bold text-sm">
                                {(user?.fullName || user?.email || "?")[0].toUpperCase()}
                            </div>
                            <span className="hidden sm:block text-sm font-medium text-gray-700 dark:text-gray-200">
                                {user?.fullName || user?.email}
                            </span>
                        </div>
                    </div>
                </header>

                {/* Page Content */}
                <main className="flex-1 overflow-y-auto p-4 sm:p-6">
                    {children}
                </main>
            </div>
        </div>
    );
}

export default function DashboardLayout({ children }) {
    return (
        <AuthProvider>
            <DashboardShell>{children}</DashboardShell>
        </AuthProvider>
    );
}
