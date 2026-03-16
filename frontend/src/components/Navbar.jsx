"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import ThemeToggle from "@/components/ThemeToggle";

const NAV_LINKS = [
    { label: "Home", href: "/" },
    { label: "About", href: "/about" },
    { label: "Product", href: "/product" },
    { label: "Features", href: "/features" },
    { label: "Pricing", href: "/pricing" },
    { label: "Contact", href: "/contact" },
];

export default function Navbar() {
    const [menuOpen, setMenuOpen] = useState(false);
    const pathname = usePathname();

    const isActive = (href) =>
        href === "/" ? pathname === "/" : pathname.startsWith(href);

    return (
        <nav className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 sticky top-0 z-50 shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">

                    {/* Logo */}
                    <Link href="/" className="text-2xl font-extrabold text-primary-600 tracking-tight">
                        Pravarthana <span className="text-gray-800 dark:text-white">HRMS</span>
                    </Link>

                    {/* Desktop Nav */}
                    <div className="hidden md:flex items-center space-x-6">
                        {NAV_LINKS.map((link) => (
                            <Link key={link.href} href={link.href}
                                className={`text-sm font-medium transition-colors duration-150 ${isActive(link.href)
                                        ? "text-primary-600 border-b-2 border-primary-600 pb-0.5"
                                        : "text-gray-600 dark:text-gray-300 hover:text-primary-600"
                                    }`}>
                                {link.label}
                            </Link>
                        ))}
                    </div>

                    {/* Desktop CTA */}
                    <div className="hidden md:flex items-center space-x-2">
                        <ThemeToggle />
                        <Link href="/login"
                            className="text-sm font-medium text-gray-700 dark:text-gray-200 hover:text-primary-600 px-3 py-2 transition">
                            Sign In
                        </Link>
                        <Link href="/demo"
                            className="text-sm font-medium bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition shadow-sm">
                            Request Demo
                        </Link>
                    </div>

                    {/* Mobile Right: theme toggle + hamburger */}
                    <div className="md:hidden flex items-center gap-1">
                        <ThemeToggle />
                        <button onClick={() => setMenuOpen(!menuOpen)}
                            className="p-2 rounded-lg text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 transition"
                            aria-label="Toggle menu">
                            {menuOpen
                                ? <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                                : <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" /></svg>}
                        </button>
                    </div>
                </div>
            </div>

            {/* Mobile Drawer */}
            {menuOpen && (
                <div className="md:hidden bg-white dark:bg-gray-900 border-t border-gray-100 dark:border-gray-800 px-4 pb-4 pt-2 space-y-1">
                    {NAV_LINKS.map((link) => (
                        <Link key={link.href} href={link.href} onClick={() => setMenuOpen(false)}
                            className={`block py-2 px-3 rounded-lg text-sm font-medium transition ${isActive(link.href)
                                    ? "text-primary-600 bg-primary-50 dark:bg-primary-900/20"
                                    : "text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-800"
                                }`}>
                            {link.label}
                        </Link>
                    ))}
                    <div className="pt-2 flex flex-col gap-2">
                        <Link href="/login" onClick={() => setMenuOpen(false)}
                            className="block text-center py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-800 transition">
                            Sign In
                        </Link>
                        <Link href="/demo" onClick={() => setMenuOpen(false)}
                            className="block text-center py-2 bg-primary-600 text-white rounded-lg text-sm font-medium hover:bg-primary-700 transition">
                            Request Demo
                        </Link>
                    </div>
                </div>
            )}
        </nav>
    );
}
