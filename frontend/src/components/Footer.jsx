import Link from "next/link";

export default function Footer() {
    return (
        <footer className="bg-gray-900 dark:bg-black text-gray-400 pt-14 pb-8">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="grid sm:grid-cols-2 md:grid-cols-4 gap-10 mb-12">

                    {/* Brand */}
                    <div>
                        <h3 className="text-white font-bold text-xl mb-3">Pravarthana HRMS</h3>
                        <p className="text-sm leading-relaxed">
                            Modern HR management platform for growing businesses. Trusted by 500+ companies.
                        </p>
                        <div className="flex gap-3 mt-5">
                            {["twitter", "linkedin", "github"].map((s) => (
                                <a key={s} href="#" aria-label={s}
                                    className="w-9 h-9 rounded-full bg-gray-800 hover:bg-primary-600 flex items-center justify-center transition">
                                    <span className="text-xs capitalize text-white">{s[0].toUpperCase()}</span>
                                </a>
                            ))}
                        </div>
                    </div>

                    {/* Product */}
                    <div>
                        <h4 className="text-white font-semibold mb-4">Product</h4>
                        <ul className="space-y-2 text-sm">
                            {[["Features", "/features"], ["Pricing", "/pricing"], ["Product Overview", "/product"], ["Request Demo", "/demo"]].map(([label, href]) => (
                                <li key={href}><Link href={href} className="hover:text-white transition">{label}</Link></li>
                            ))}
                        </ul>
                    </div>

                    {/* Company */}
                    <div>
                        <h4 className="text-white font-semibold mb-4">Company</h4>
                        <ul className="space-y-2 text-sm">
                            {[["About Us", "/about"], ["Contact", "/contact"], ["Sign In", "/login"], ["Register", "/register"]].map(([label, href]) => (
                                <li key={href}><Link href={href} className="hover:text-white transition">{label}</Link></li>
                            ))}
                        </ul>
                    </div>

                    {/* Legal */}
                    <div>
                        <h4 className="text-white font-semibold mb-4">Legal</h4>
                        <ul className="space-y-2 text-sm">
                            {[["Privacy Policy", "/privacy"], ["Terms of Service", "/terms"], ["Security", "/security"]].map(([label, href]) => (
                                <li key={href}><Link href={href} className="hover:text-white transition">{label}</Link></li>
                            ))}
                        </ul>
                    </div>
                </div>

                <div className="border-t border-gray-800 pt-6 flex flex-col sm:flex-row justify-between items-center text-sm gap-3">
                    <p>© {new Date().getFullYear()} Pravarthana Technologies Pvt Ltd. All rights reserved.</p>
                    <p className="text-gray-600">Built with ❤️ for Indian enterprises</p>
                </div>
            </div>
        </footer>
    );
}
