import Link from "next/link";
import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";

export const metadata = {
    title: "About Us — Pravarthana HRMS",
    description: "Learn about Pravarthana Technologies' mission, vision, and values in simplifying HR for Indian enterprises.",
};

const VALUES = [
    { icon: "🎯", title: "Customer First", desc: "Every feature exists to make HR professionals' lives easier." },
    { icon: "🔒", title: "Security", desc: "Enterprise-grade security with role-based access and audit trails." },
    { icon: "⚡", title: "Performance", desc: "99.9% uptime SLA with blazing-fast response times." },
    { icon: "🌱", title: "Continuous Growth", desc: "We ship improvements every sprint based on real user feedback." },
];

export default function AboutPage() {
    return (
        <div className="min-h-screen bg-white dark:bg-gray-950">
            <Navbar />

            {/* Hero */}
            <section className="bg-gradient-to-br from-primary-600 to-primary-800 py-24 px-4 text-center">
                <h1 className="text-5xl font-extrabold text-white mb-4">About Pravarthana</h1>
                <p className="text-xl text-primary-100 max-w-2xl mx-auto">
                    We are on a mission to make HR simple, scalable, and human — for every Indian business.
                </p>
            </section>

            {/* Mission & Vision */}
            <section className="max-w-5xl mx-auto px-4 py-20 grid md:grid-cols-2 gap-12">
                <div>
                    <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">Our Mission</h2>
                    <p className="text-gray-600 dark:text-gray-300 text-lg leading-relaxed">
                        To empower HR teams with intelligent tools that eliminate manual work, reduce errors, and create a better employee experience — from day one to departure.
                    </p>
                </div>
                <div>
                    <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">Our Vision</h2>
                    <p className="text-gray-600 dark:text-gray-300 text-lg leading-relaxed">
                        To become India's most trusted HRMS platform — one that grows with every company, from 10 employees to 10,000.
                    </p>
                </div>
            </section>

            {/* Why Choose Us */}
            <section className="bg-gray-50 dark:bg-gray-900 py-20 px-4">
                <div className="max-w-5xl mx-auto">
                    <h2 className="text-3xl font-bold text-center text-gray-900 dark:text-white mb-12">Why Pravarthana HRMS?</h2>
                    <div className="grid sm:grid-cols-3 gap-8 text-center">
                        {[
                            { stat: "500+", label: "Companies Trust Us" },
                            { stat: "50,000+", label: "Employees Managed" },
                            { stat: "< 24h", label: "Average Onboarding Time" },
                        ].map((item) => (
                            <div key={item.label} className="bg-white dark:bg-gray-800 rounded-2xl p-8 shadow-sm">
                                <p className="text-4xl font-extrabold text-primary-600 mb-2">{item.stat}</p>
                                <p className="text-gray-600 dark:text-gray-300">{item.label}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Values */}
            <section className="max-w-5xl mx-auto px-4 py-20">
                <h2 className="text-3xl font-bold text-center text-gray-900 dark:text-white mb-12">Our Values</h2>
                <div className="grid sm:grid-cols-2 gap-6">
                    {VALUES.map((v) => (
                        <div key={v.title} className="flex gap-4 p-6 bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-2xl shadow-sm hover:shadow-md transition">
                            <span className="text-3xl">{v.icon}</span>
                            <div>
                                <h3 className="font-bold text-gray-900 dark:text-white mb-1">{v.title}</h3>
                                <p className="text-gray-500 dark:text-gray-400 text-sm">{v.desc}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            {/* CTA */}
            <section className="bg-primary-600 py-16 px-4 text-center">
                <h2 className="text-3xl font-bold text-white mb-4">Ready to join 500+ companies?</h2>
                <Link href="/demo" className="inline-block bg-white text-primary-600 px-8 py-3 rounded-xl font-semibold hover:bg-primary-50 transition mt-2">
                    Request a Demo
                </Link>
            </section>

            <Footer />
        </div>
    );
}
