import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";
import PricingCard from "@/components/PricingCard";

export const metadata = {
    title: "Pricing — Pravarthana HRMS",
    description: "Simple, transparent pricing for companies of all sizes. Starter, Professional, and Enterprise plans.",
};

const PLANS = [
    {
        tier: "Starter",
        price: "₹999",
        description: "Perfect for small teams just getting started",
        features: [
            "Up to 25 employees",
            "Attendance tracking",
            "Leave management",
            "Basic reports",
            "Email support",
        ],
        cta: "Start Free Trial",
        href: "/register",
    },
    {
        tier: "Professional",
        price: "₹2,499",
        description: "For growing companies that need more power",
        features: [
            "Up to 200 employees",
            "Everything in Starter",
            "Payroll processing",
            "Advanced analytics",
            "Team communication",
            "Priority support",
        ],
        cta: "Get Professional",
        href: "/demo",
        highlighted: true,
    },
    {
        tier: "Enterprise",
        price: "Custom",
        period: "",
        description: "Tailored for large organizations",
        features: [
            "Unlimited employees",
            "Everything in Professional",
            "Dedicated account manager",
            "Custom integrations",
            "On-premise deployment option",
            "SLA guarantee",
            "24/7 phone support",
        ],
        cta: "Contact Sales",
        href: "/contact",
    },
];

export default function PricingPage() {
    return (
        <div className="min-h-screen bg-white dark:bg-gray-950">
            <Navbar />

            {/* Hero */}
            <section className="py-20 px-4 text-center bg-gradient-to-b from-gray-50 to-white dark:from-gray-900 dark:to-gray-950">
                <h1 className="text-5xl font-extrabold text-gray-900 dark:text-white mb-4">Simple, Transparent Pricing</h1>
                <p className="text-xl text-gray-500 dark:text-gray-400 max-w-xl mx-auto">
                    No hidden fees. No per-user surprises. Pick the plan that fits your team.
                </p>
            </section>

            {/* Plans */}
            <section className="max-w-6xl mx-auto px-4 pb-24 pt-4">
                <div className="grid md:grid-cols-3 gap-8 items-start">
                    {PLANS.map((p) => <PricingCard key={p.tier} {...p} />)}
                </div>
            </section>

            {/* FAQ */}
            <section className="bg-gray-50 dark:bg-gray-900 py-20 px-4">
                <div className="max-w-3xl mx-auto">
                    <h2 className="text-3xl font-bold text-center text-gray-900 dark:text-white mb-12">Frequently Asked</h2>
                    {[
                        ["Is there a free trial?", "Yes! Starter plan includes a 14-day free trial, no credit card required."],
                        ["Can I upgrade anytime?", "Absolutely. You can upgrade or downgrade your plan at any time from the admin panel."],
                        ["What payment methods do you accept?", "We accept UPI, net banking, credit/debit cards, and NEFT/RTGS for Enterprise."],
                        ["Is data stored securely?", "Yes. All data is encrypted at rest and in transit. We are SOC 2 compliant."],
                    ].map(([q, a]) => (
                        <div key={q} className="mb-6 bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700">
                            <h3 className="font-semibold text-gray-900 dark:text-white mb-2">{q}</h3>
                            <p className="text-gray-500 dark:text-gray-400 text-sm">{a}</p>
                        </div>
                    ))}
                </div>
            </section>

            <Footer />
        </div>
    );
}
