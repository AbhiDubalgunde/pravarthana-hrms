/**
 * employeeService.js — Employee API service layer.
 * Import this instead of using api.js directly for employee operations.
 */
import api from './api';

export const employeeService = {
    /** GET /api/employees?page=0&size=20&search=&department=&status= */
    getAll: (params = {}) => api.get('/employees', { params }),

    /** GET /api/employees/:id */
    getById: (id) => api.get(`/employees/${id}`),

    /** GET /api/employees/me — current logged-in user's employee profile */
    getMe: () => api.get('/employees/me'),

    /** GET /api/employees/my-team — team members of current manager */
    getMyTeam: () => api.get('/employees/my-team'),

    /** POST /api/employees */
    create: (data) => api.post('/employees', data),

    /** PUT /api/employees/:id */
    update: (id, data) => api.put(`/employees/${id}`, data),

    /** DELETE /api/employees/:id */
    delete: (id) => api.delete(`/employees/${id}`),
};

export default employeeService;
