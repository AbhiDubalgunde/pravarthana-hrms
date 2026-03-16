"use client";

import { useState } from "react";
import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";

export default function ContactPage() {
    const [form, setForm] = useState({ name: "", email: "", company: "", message: "" });
    const [submitted, setSubmitted] = useState(false);

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        // TODO: Connect to backend /api/contact or email service
        setSubmitted(true);
    };

    return (
        <div className="min-h-screen bg-white dark:bg-gray-950">
            <Navbar />

            {/* Hero */}
            <section className="bg-gradient-to-br from-primary-600 to-primary-800 py-20 px-4 text-center">
                <h1 className="text-5xl font-extrabold text-white mb-3">Get In Touch</h1>
                <p className="text-primary-100 text-xl max-w-xl mx-auto">Have a question or want a personalised walkthrough? We are here to help.</p>
            </section>

            <section className="max-w-6xl mx-auto px-4 py-20 grid md:grid-cols-2 gap-16">

                {/* Form */}
                <div>
                    <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">Send us a message</h2>
                    {submitted ? (
                        <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-2xl p-8 text-center">
                            <p className="text-4xl mb-4">✅</p>
                            <h3 className="text-xl font-bold text-green-800 dark:text-green-200 mb-2">Message received!</h3>
                            <p className="text-green-700 dark:text-green-300">We will get back to you within 1 business day.</p>
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-5">
                            {[
                                { name: "name", label: "Your Name", type: "text", placeholder: "Rahul Sharma" },
                                { name: "email", label: "Email Address", type: "email", placeholder: "rahul@company.com" },
                                { name: "company", label: "Company", type: "text", placeholder: "Acme Technologies" },
                            ].map(({ name, label, type, placeholder }) => (
                                <div key={name}>
                                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">{label}</label>
                                    <input
                                        type={type}
                                        name={name}
                                        value={form[name]}
                                        onChange={handleChange}
                                        placeholder={placeholder}
                                        required
                                        className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-primary-500 transition"
                                    />
                                </div>
                            ))}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">Message</label>
                                <textarea
                                    name="message"
                                    rows={5}
                                    value={form.message}
                                    onChange={handleChange}
                                    placeholder="How can we help you?"
                                    required
                                    className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-primary-500 transition resize-none"
                                />
                            </div>
                            <button type="submit"
                                className="w-full bg-primary-600 text-white py-3 rounded-xl font-semibold hover:bg-primary-700 transition shadow-md">
                                Send Message
                            </button>
                        </form>
                    )}
                </div>

                {/* Contact Info */}
                <div className="space-y-8">
                    <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Contact Details</h2>
                    {[
                        { icon: "📧", label: "Email", value: "hello@pravarthana.com" },
                        { icon: "📞", label: "Phone", value: "+91 98765 43210" },
                        { icon: "📍", label: "Office", value: "Bangalore, Karnataka, India" },
                        { icon: "⏰", label: "Hours", value: "Mon–Fri, 9 AM – 6 PM IST" },
                    ].map((c) => (
                        <div key={c.label} className="flex items-start gap-4">
                            <span className="text-2xl">{c.icon}</span>
                            <div>
                                <p className="text-sm font-semibold text-gray-500 dark:text-gray-400">{c.label}</p>
                                <p className="text-gray-900 dark:text-white font-medium">{c.value}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            <Footer />
        </div>
    );
}
