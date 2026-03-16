export default function FeatureCard({ icon, title, description, color = "bg-primary-600" }) {
    return (
        <div className="group bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 p-8 rounded-2xl shadow-sm hover:shadow-lg transition-all duration-300 hover:-translate-y-1">
            <div className={`w-12 h-12 ${color} rounded-xl mb-5 flex items-center justify-center shadow-md`}>
                {icon}
            </div>
            <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-2">{title}</h3>
            <p className="text-gray-500 dark:text-gray-400 text-sm leading-relaxed">{description}</p>
        </div>
    );
}
