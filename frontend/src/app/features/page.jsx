import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";
import FeatureCard from "@/components/FeatureCard";
import Link from "next/link";

export const metadata = {
    title: "Features — Pravarthana HRMS",
    description: "Explore role-based access, analytics, leave automation, team management, and secure authentication in Pravarthana HRMS.",
};

const FEATURES = [
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>,
        title: "Role-Based Access Control",
        description: "Super Admin, HR Admin, Team Lead, and Employee roles with fine-grained permission control.",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>,
        title: "Real-Time Analytics",
        description: "Live dashboards with headcount, attendance rates, leave trends, and payroll summaries.",
        color: "bg-indigo-600",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>,
        title: "Leave Automation",
        description: "Auto-approve policies, configurable types, team calendar, and email notifications.",
        color: "bg-emerald-600",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" /></svg>,
        title: "Team Management",
        description: "Team leads can view their members, track attendance, approve leaves, and send updates.",
        color: "bg-amber-600",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>,
        title: "Secure Authentication",
        description: "JWT-based auth, BCrypt password hashing, role-based API protection, and middleware guards.",
        color: "bg-red-600",
    },
    {
        icon: <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
        title: "Payroll Engine",
        description: "Automated CTC breakdown, PF, ESI, TDS, payslip generation, and bank export.",
        color: "bg-sky-600",
    },
];

export default function FeaturesPage() {
    return (
        <div className="min-h-screen bg-white dark:bg-gray-950">
            <Navbar />

            {/* Hero */}
            <section className="bg-gradient-to-br from-indigo-600 to-primary-700 py-24 px-4 text-center">
                <h1 className="text-5xl font-extrabold text-white mb-4">Packed with Features</h1>
                <p className="text-xl text-indigo-100 max-w-2xl mx-auto">
                    Everything your HR team needs — in one platform, with zero compromise on security or usability.
                </p>
            </section>

            {/* Feature Cards */}
            <section className="max-w-7xl mx-auto px-4 py-20">
                <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    {FEATURES.map((f) => <FeatureCard key={f.title} {...f} />)}
                </div>
            </section>

            {/* Feature Comparison Table */}
            <section className="bg-gray-50 dark:bg-gray-900 py-20 px-4">
                <div className="max-w-4xl mx-auto">
                    <h2 className="text-3xl font-bold text-center text-gray-900 dark:text-white mb-12">Role Capabilities</h2>
                    <div className="overflow-x-auto rounded-2xl shadow">
                        <table className="w-full text-sm bg-white dark:bg-gray-800">
                            <thead className="bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-200">
                                <tr>
                                    <th className="px-6 py-4 text-left font-semibold">Capability</th>
                                    {["Super Admin", "HR Admin", "Team Lead", "Employee"].map(r => (
                                        <th key={r} className="px-4 py-4 text-center font-semibold">{r}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                                {[
                                    ["View Employees", true, true, false, false],
                                    ["Manage Payroll", true, true, false, false],
                                    ["Approve Leaves", true, true, true, false],
                                    ["View Team Attendance", true, true, true, false],
                                    ["View Own Attendance", true, true, true, true],
                                    ["Apply Leave", true, true, true, true],
                                    ["Access Reports", true, true, false, false],
                                    ["System Settings", true, false, false, false],
                                ].map(([cap, ...marks]) => (
                                    <tr key={cap} className="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition">
                                        <td className="px-6 py-4 text-gray-900 dark:text-gray-100 font-medium">{cap}</td>
                                        {marks.map((m, i) => (
                                            <td key={i} className="px-4 py-4 text-center">
                                                {m ? <span className="text-emerald-500 font-bold text-lg">✓</span>
                                                    : <span className="text-gray-300 dark:text-gray-600">—</span>}
                                            </td>
                                        ))}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>

            {/* CTA */}
            <section className="py-20 px-4 text-center">
                <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">Want to see it live?</h2>
                <Link href="/demo" className="inline-block bg-primary-600 text-white px-8 py-4 rounded-xl font-semibold hover:bg-primary-700 transition shadow-lg">
                    Request a Demo
                </Link>
            </section>

            <Footer />
        </div>
    );
}
