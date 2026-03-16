import api from "@/services/api";

// ── Department service ──────────────────────────────────────────────────────
export const departmentService = {
    /** GET /api/departments — returns array of { id, name, description, employeeCount } */
    getAll: () => api.get("/departments"),

    /** GET /api/departments/{id} — single dept with teams list */
    getById: (id) => api.get(`/departments/${id}`),

    /** POST /api/departments */
    create: (payload) => api.post("/departments", payload),

    /** PUT /api/departments/{id} */
    update: (id, payload) => api.put(`/departments/${id}`, payload),

    /** DELETE /api/departments/{id} */
    delete: (id) => api.delete(`/departments/${id}`),
};
