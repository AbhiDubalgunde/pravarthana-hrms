import Link from "next/link";

export default function PricingCard({ tier, price, period = "/month", description, features, cta = "Get Started", href = "/demo", highlighted = false }) {
    return (
        <div className={`relative flex flex-col rounded-2xl p-8 shadow-sm border transition-all duration-300 hover:shadow-xl hover:-translate-y-1 ${highlighted
                ? "bg-primary-600 text-white border-primary-500 scale-105"
                : "bg-white dark:bg-gray-800 text-gray-900 dark:text-white border-gray-200 dark:border-gray-700"
            }`}>
            {highlighted && (
                <div className="absolute -top-4 left-1/2 -translate-x-1/2 bg-yellow-400 text-yellow-900 text-xs font-bold px-4 py-1 rounded-full shadow">
                    MOST POPULAR
                </div>
            )}
            <div className="mb-6">
                <h3 className={`text-xl font-bold mb-1 ${highlighted ? "text-white" : "text-gray-900 dark:text-white"}`}>{tier}</h3>
                <p className={`text-sm mb-4 ${highlighted ? "text-primary-100" : "text-gray-500 dark:text-gray-400"}`}>{description}</p>
                <div className="flex items-end gap-1">
                    <span className={`text-4xl font-extrabold ${highlighted ? "text-white" : "text-gray-900 dark:text-white"}`}>{price}</span>
                    {period && <span className={`text-sm mb-1 ${highlighted ? "text-primary-100" : "text-gray-500"}`}>{period}</span>}
                </div>
            </div>

            <ul className="space-y-3 flex-1 mb-8">
                {features.map((f, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm">
                        <svg className={`w-5 h-5 mt-0.5 flex-shrink-0 ${highlighted ? "text-primary-200" : "text-primary-500"}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                        <span className={highlighted ? "text-primary-50" : "text-gray-600 dark:text-gray-300"}>{f}</span>
                    </li>
                ))}
            </ul>

            <Link href={href}
                className={`w-full text-center py-3 rounded-xl font-semibold text-sm transition ${highlighted
                        ? "bg-white text-primary-600 hover:bg-primary-50"
                        : "bg-primary-600 text-white hover:bg-primary-700"
                    }`}>
                {cta}
            </Link>
        </div>
    );
}
