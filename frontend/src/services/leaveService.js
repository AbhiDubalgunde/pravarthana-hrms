/**
 * leaveService.js — Leave management API service layer.
 */
import api from './api';

export const leaveService = {
    /** GET /api/leaves — all leaves (HR/Admin view) */
    getAll: (params = {}) => api.get('/leaves', { params }),

    /** GET /api/leaves/my — current user's leave requests */
    getMy: () => api.get('/leaves/my'),

    /** GET /api/leaves/:id */
    getById: (id) => api.get(`/leaves/${id}`),

    /**
     * POST /api/leaves — submit a new leave request
     * @param {{ leaveType, startDate, endDate, reason }} data
     */
    create: (data) => api.post('/leaves', data),

    /**
     * PUT /api/leaves/:id/approve — approve a leave request (HR/Admin)
     */
    approve: (id) => api.put(`/leaves/${id}/approve`),

    /**
     * PUT /api/leaves/:id/reject — reject a leave request (HR/Admin)
     * @param {{ reason }} data
     */
    reject: (id, data) => api.put(`/leaves/${id}/reject`, data),

    /** DELETE /api/leaves/:id — cancel/delete a leave request */
    cancel: (id) => api.delete(`/leaves/${id}`),

    /** GET /api/leaves/balance — current user's leave balance */
    getBalance: () => api.get('/leaves/balance'),
};

export default leaveService;
