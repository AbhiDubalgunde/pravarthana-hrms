import Link from "next/link";
import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";

export const metadata = {
    title: "Product Overview — Pravarthana HRMS",
    description: "Explore the complete Pravarthana HRMS product — all modules, features, and the HR dashboard built for modern teams.",
};

const MODULES = [
    { icon: "👥", title: "Employee Management", desc: "Digital employee records, onboarding workflows, org chart, and document management." },
    { icon: "📅", title: "Attendance & Time", desc: "Biometric/geo-tagged check-in, shift scheduling, overtime calculations, and absence tracking." },
    { icon: "📝", title: "Leave Management", desc: "Configurable leave types, team calendar, approval workflows, and balance reports." },
    { icon: "💰", title: "Payroll Processing", desc: "Automated salary calculation, tax deductions, payslip generation, and statutory compliance." },
    { icon: "📊", title: "Reports & Analytics", desc: "50+ pre-built reports, executive dashboards, and exportable Excel/PDF outputs." },
    { icon: "💬", title: "Team Communication", desc: "Real-time chat, announcements, polls, and HR broadcast messaging." },
];

export default function ProductPage() {
    return (
        <div className="min-h-screen bg-white dark:bg-gray-950">
            <Navbar />

            {/* Hero */}
            <section className="bg-gradient-to-br from-gray-900 to-gray-800 py-24 px-4 text-center">
                <h1 className="text-5xl font-extrabold text-white mb-4">One Platform. All of HR.</h1>
                <p className="text-xl text-gray-300 max-w-2xl mx-auto mb-8">
                    From hiring to retirement — Pravarthana HRMS manages every HR touchpoint in one unified system.
                </p>
                <div className="flex justify-center gap-4">
                    <Link href="/demo" className="bg-primary-600 text-white px-7 py-3 rounded-xl font-semibold hover:bg-primary-700 transition">
                        See It Live →
                    </Link>
                    <Link href="/features" className="bg-white/10 text-white px-7 py-3 rounded-xl font-semibold border border-white/20 hover:bg-white/20 transition">
                        All Features
                    </Link>
                </div>
            </section>

            {/* Dashboard Preview Placeholder */}
            <section className="max-w-6xl mx-auto px-4 py-20">
                <h2 className="text-3xl font-bold text-center text-gray-900 dark:text-white mb-4">HR Dashboard Overview</h2>
                <p className="text-center text-gray-500 dark:text-gray-400 mb-12">A single view to manage your entire workforce</p>
                <div className="bg-gradient-to-br from-primary-50 to-indigo-50 dark:from-gray-800 dark:to-gray-900 border border-primary-100 dark:border-gray-700 rounded-3xl p-12 flex items-center justify-center min-h-[320px] shadow-inner">
                    <div className="text-center text-gray-400 dark:text-gray-500">
                        <div className="text-6xl mb-4">📊</div>
                        <p className="text-lg font-medium">Dashboard Screenshot</p>
                        <p className="text-sm mt-1">Available after login</p>
                        <Link href="/login" className="mt-6 inline-block bg-primary-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-primary-700 transition text-sm">
                            Sign In to View
                        </Link>
                    </div>
                </div>
            </section>

            {/* Modules */}
            <section className="bg-gray-50 dark:bg-gray-900 py-20 px-4">
                <div className="max-w-6xl mx-auto">
                    <h2 className="text-3xl font-bold text-center text-gray-900 dark:text-white mb-12">Module Breakdown</h2>
                    <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
                        {MODULES.map((m) => (
                            <div key={m.title} className="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700 hover:shadow-md transition">
                                <span className="text-3xl mb-4 block">{m.icon}</span>
                                <h3 className="font-bold text-gray-900 dark:text-white mb-2">{m.title}</h3>
                                <p className="text-sm text-gray-500 dark:text-gray-400 leading-relaxed">{m.desc}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* CTA */}
            <section className="py-20 px-4 text-center">
                <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">See the full product in action</h2>
                <Link href="/demo" className="inline-block bg-primary-600 text-white px-8 py-4 rounded-xl font-semibold hover:bg-primary-700 transition shadow-lg mt-2">
                    Book a Free Demo
                </Link>
            </section>

            <Footer />
        </div>
    );
}
