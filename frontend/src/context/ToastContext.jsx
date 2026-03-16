"use client";

import { createContext, useContext, useState, useCallback } from "react";

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
    const [toasts, setToasts] = useState([]);

    const addToast = useCallback((message, type = "info", duration = 3500) => {
        const id = Date.now();
        setToasts((prev) => [...prev, { id, message, type }]);
        setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), duration);
    }, []);

    const toast = {
        success: (msg) => addToast(msg, "success"),
        error: (msg) => addToast(msg, "error"),
        info: (msg) => addToast(msg, "info"),
    };

    return (
        <ToastContext.Provider value={toast}>
            {children}
            {/* Toast container */}
            <div className="fixed bottom-5 right-5 z-[9999] flex flex-col gap-2 min-w-[280px]">
                {toasts.map((t) => (
                    <div key={t.id}
                        className={`flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg text-sm font-medium animate-slide-in transition-all
              ${t.type === "success" ? "bg-emerald-600 text-white" :
                                t.type === "error" ? "bg-red-600 text-white" :
                                    "bg-gray-800 text-white"}`}>
                        <span>
                            {t.type === "success" ? "✅" : t.type === "error" ? "❌" : "ℹ️"}
                        </span>
                        <span>{t.message}</span>
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    );
}

export const useToast = () => {
    const ctx = useContext(ToastContext);
    if (!ctx) throw new Error("useToast must be inside ToastProvider");
    return ctx;
};
