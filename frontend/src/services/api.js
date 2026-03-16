import axios from "axios";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081/api";

// ── Axios instance with JWT header ────────────────────────────────
const api = axios.create({ baseURL: BASE_URL });

api.interceptors.request.use((config) => {
    if (typeof window !== "undefined") {
        const token = localStorage.getItem("token");
        if (token) config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    (res) => res,
    (err) => {
        if (err.response?.status === 401) {
            // Token expired — clear and redirect
            if (typeof window !== "undefined") {
                localStorage.removeItem("token");
                localStorage.removeItem("user");
                document.cookie = "hrms_token=; path=/; max-age=0";
                window.location.href = "/login";
            }
        }
        return Promise.reject(err);
    }
);

// ── Employee Service ───────────────────────────────────────────────
export const employeeService = {
    getAll: (params = {}) => api.get("/employees", { params }),
    getById: (id) => api.get(`/employees/${id}`),
    getMe: () => api.get("/employees/me"),
    getMyTeam: () => api.get("/employees/my-team"),
    create: (data) => api.post("/employees", data),
    update: (id, data) => api.put(`/employees/${id}`, data),
    delete: (id) => api.delete(`/employees/${id}`),
};

// ── Attendance Service ─────────────────────────────────────────────
export const attendanceService = {
    punchIn: () => api.post("/attendance/punch-in"),
    punchOut: () => api.post("/attendance/punch-out"),
    getByEmployee: (employeeId) => api.get(`/attendance/employee/${employeeId}`),
    getMonthly: (employeeId, month) =>
        api.get(`/attendance/monthly/${employeeId}`, { params: { month } }),
    getToday: (employeeId) => api.get(`/attendance/today/${employeeId}`),
};

export default api;
