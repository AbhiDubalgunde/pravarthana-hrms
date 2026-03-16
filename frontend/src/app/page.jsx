import Link from "next/link";
import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";
import FeatureCard from "@/components/FeatureCard";

export const metadata = {
    title: "Pravarthana HRMS - Modern HR Management Simplified",
    description: "Streamline HR operations with employee management, attendance, leave, payroll and team communication — all in one platform.",
};

const FEATURES = [
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" /></svg>,
        title: "Employee Management",
        description: "Complete employee lifecycle from onboarding to exit. Digital files, org charts, and role assignments.",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
        title: "Attendance Tracking",
        description: "Real-time attendance with geo-tagging, shift management, and automated detailed reports.",
        color: "bg-indigo-600",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>,
        title: "Leave Management",
        description: "Automated leave workflows, balance tracking, and team leave calendar at a glance.",
        color: "bg-emerald-600",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
        title: "Payroll",
        description: "Accurate payroll processing with tax calculations, payslips, and compliance reports.",
        color: "bg-amber-600",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>,
        title: "Team Chat",
        description: "Real-time messaging, announcements, and file sharing for seamless collaboration.",
        color: "bg-sky-600",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>,
        title: "Analytics & Reports",
        description: "Executive dashboards, headcount trends, and exportable reports for data-driven decisions.",
        color: "bg-purple-600",
    },
];

const STATS = [
    { value: "500+", label: "Companies" },
    { value: "50K+", label: "Employees Managed" },
    { value: "99.9%", label: "Uptime SLA" },
    { value: "4.9★", label: "Customer Rating" },
];

export default function HomePage() {
    return (
        <div className="min-h-screen bg-gradient-to-b from-white to-gray-50 dark:from-gray-950 dark:to-gray-900">
            <Navbar />

            {/* ── Hero ───────────────────────────────────── */}
            <section className="relative pt-20 pb-28 px-4 sm:px-6 lg:px-8 overflow-hidden">
                {/* background blob */}
                <div className="absolute inset-0 -z-10 overflow-hidden">
                    <div className="absolute -top-40 -right-40 w-[600px] h-[600px] bg-primary-100 dark:bg-primary-900/20 rounded-full blur-3xl opacity-60" />
                    <div className="absolute -bottom-20 -left-20 w-[400px] h-[400px] bg-indigo-100 dark:bg-indigo-900/20 rounded-full blur-3xl opacity-40" />
                </div>

                <div className="max-w-5xl mx-auto text-center">
                    <span className="inline-block mb-4 px-4 py-1.5 bg-primary-100 dark:bg-primary-900/40 text-primary-700 dark:text-primary-300 text-sm font-semibold rounded-full">
                        🚀 Now in Production — Trusted by 500+ Indian Companies
                    </span>
                    <h1 className="text-5xl md:text-6xl lg:text-7xl font-extrabold text-gray-900 dark:text-white mb-6 leading-tight">
                        Modern HR Management
                        <span className="block text-primary-600"> Simplified.</span>
                    </h1>
                    <p className="text-xl text-gray-600 dark:text-gray-300 mb-10 max-w-2xl mx-auto leading-relaxed">
                        Manage employees, track attendance, automate payroll, and collaborate — all from one beautiful platform built for Indian enterprises.
                    </p>
                    <div className="flex flex-col sm:flex-row justify-center gap-4">
                        <Link href="/register"
                            className="bg-primary-600 text-white px-8 py-4 rounded-xl text-lg font-semibold hover:bg-primary-700 transition shadow-lg shadow-primary-200 dark:shadow-none">
                            Get Started Free →
                        </Link>
                        <Link href="/product"
                            className="bg-white dark:bg-gray-800 text-primary-600 dark:text-primary-400 px-8 py-4 rounded-xl text-lg font-semibold border-2 border-primary-200 dark:border-primary-800 hover:border-primary-400 transition">
                            Learn More
                        </Link>
                    </div>
                </div>
            </section>

            {/* ── Stats ──────────────────────────────────── */}
            <section className="py-16 bg-primary-600">
                <div className="max-w-5xl mx-auto px-4 grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
                    {STATS.map((s) => (
                        <div key={s.label}>
                            <p className="text-4xl font-extrabold text-white mb-1">{s.value}</p>
                            <p className="text-primary-200 text-sm font-medium">{s.label}</p>
                        </div>
                    ))}
                </div>
            </section>

            {/* ── Features ───────────────────────────────── */}
            <section className="py-24 px-4 sm:px-6 lg:px-8">
                <div className="max-w-7xl mx-auto">
                    <div className="text-center mb-16">
                        <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">Everything You Need</h2>
                        <p className="text-xl text-gray-500 dark:text-gray-400">Powerful features designed for modern HR teams</p>
                    </div>
                    <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
                        {FEATURES.map((f) => (
                            <FeatureCard key={f.title} {...f} />
                        ))}
                    </div>
                </div>
            </section>

            {/* ── CTA ────────────────────────────────────── */}
            <section className="py-24 px-4 bg-gray-900 dark:bg-black">
                <div className="max-w-3xl mx-auto text-center">
                    <h2 className="text-4xl font-bold text-white mb-4">Ready to Transform Your HR?</h2>
                    <p className="text-gray-400 text-lg mb-10">Join 500+ companies that streamline HR with Pravarthana HRMS</p>
                    <div className="flex flex-col sm:flex-row justify-center gap-4">
                        <Link href="/demo"
                            className="bg-primary-600 text-white px-8 py-4 rounded-xl font-semibold hover:bg-primary-700 transition shadow-lg">
                            Request a Demo
                        </Link>
                        <Link href="/pricing"
                            className="bg-white/10 text-white px-8 py-4 rounded-xl font-semibold border border-white/20 hover:bg-white/20 transition">
                            View Pricing
                        </Link>
                    </div>
                </div>
            </section>

            <Footer />
        </div>
    );
}
