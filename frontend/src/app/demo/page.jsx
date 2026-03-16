"use client";

import { useState } from "react";
import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";

const SIZES = ["1–10", "11–50", "51–200", "201–500", "500+"];
const TIMES = ["9:00 AM – 11:00 AM IST", "11:00 AM – 1:00 PM IST", "2:00 PM – 4:00 PM IST", "4:00 PM – 6:00 PM IST"];

export default function DemoPage() {
    const [form, setForm] = useState({ name: "", email: "", phone: "", company: "", size: SIZES[0], time: TIMES[0], notes: "" });
    const [submitted, setSubmitted] = useState(false);

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = (e) => {
        e.preventDefault();
        // TODO: Send to /api/demo requests or CRM
        setSubmitted(true);
    };

    return (
        <div className="min-h-screen bg-white dark:bg-gray-950">
            <Navbar />

            {/* Hero */}
            <section className="bg-gradient-to-br from-gray-900 to-gray-800 py-20 px-4 text-center">
                <h1 className="text-5xl font-extrabold text-white mb-4">See Pravarthana HRMS in Action</h1>
                <p className="text-gray-300 text-xl max-w-xl mx-auto">
                    Book a personalised 30-minute demo with our product team. No sales pressure — just answers.
                </p>
            </section>

            <section className="max-w-5xl mx-auto px-4 py-20 grid md:grid-cols-2 gap-16">

                {/* Form */}
                <div>
                    <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">Book Your Demo</h2>
                    {submitted ? (
                        <div className="bg-primary-50 dark:bg-primary-900/20 border border-primary-200 dark:border-primary-800 rounded-2xl p-8 text-center">
                            <p className="text-4xl mb-3">🎉</p>
                            <h3 className="text-xl font-bold text-primary-800 dark:text-primary-200 mb-2">Demo booked!</h3>
                            <p className="text-primary-700 dark:text-primary-300">Our team will confirm your slot via email within 2 hours.</p>
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-4">
                            {[
                                { name: "name", label: "Full Name", type: "text", placeholder: "Priya Agarwal" },
                                { name: "email", label: "Work Email", type: "email", placeholder: "priya@company.com" },
                                { name: "phone", label: "Phone Number", type: "tel", placeholder: "+91 98765 43210" },
                                { name: "company", label: "Company Name", type: "text", placeholder: "Acme Infosystems" },
                            ].map(({ name, label, type, placeholder }) => (
                                <div key={name}>
                                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">{label}</label>
                                    <input type={type} name={name} value={form[name]} onChange={handleChange} placeholder={placeholder} required
                                        className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-primary-500 transition" />
                                </div>
                            ))}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">Business Size</label>
                                <select name="size" value={form.size} onChange={handleChange}
                                    className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-primary-500 transition">
                                    {SIZES.map((s) => <option key={s} value={s}>{s} employees</option>)}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">Preferred Time (IST)</label>
                                <select name="time" value={form.time} onChange={handleChange}
                                    className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-primary-500 transition">
                                    {TIMES.map((t) => <option key={t} value={t}>{t}</option>)}
                                </select>
                            </div>
                            <button type="submit" className="w-full bg-primary-600 text-white py-3 rounded-xl font-semibold hover:bg-primary-700 transition shadow-md">
                                Book Demo →
                            </button>
                        </form>
                    )}
                </div>

                {/* What to Expect */}
                <div className="space-y-6">
                    <h2 className="text-2xl font-bold text-gray-900 dark:text-white">What to Expect</h2>
                    {[
                        { icon: "⏱", title: "30 Minutes", desc: "Focused walkthrough of the features most relevant to you." },
                        { icon: "🎯", title: "Tailored Demo", desc: "We customise the demo based on your company size and challenges." },
                        { icon: "💬", title: "Q&A Session", desc: "Ask anything — pricing, integrations, data migration, compliance." },
                        { icon: "📋", title: "No Obligation", desc: "Zero pressure. You'll leave with clarity, not a sales contract." },
                    ].map((item) => (
                        <div key={item.title} className="flex gap-4 p-5 bg-gray-50 dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700">
                            <span className="text-2xl">{item.icon}</span>
                            <div>
                                <h3 className="font-semibold text-gray-900 dark:text-white">{item.title}</h3>
                                <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{item.desc}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            <Footer />
        </div>
    );
}
