/**
 * attendanceService.js — Attendance API service layer.
 * Import this instead of using api.js directly for attendance operations.
 */
import api from './api';

export const attendanceService = {
    /** POST /api/attendance/punch-in */
    punchIn: () => api.post('/attendance/punch-in'),

    /** POST /api/attendance/punch-out */
    punchOut: () => api.post('/attendance/punch-out'),

    /** GET /api/attendance/employee/:employeeId */
    getByEmployee: (employeeId) => api.get(`/attendance/employee/${employeeId}`),

    /**
     * GET /api/attendance/monthly/:employeeId?month=YYYY-MM
     * @param {string|number} employeeId
     * @param {string} month — format "YYYY-MM"
     */
    getMonthly: (employeeId, month) =>
        api.get(`/attendance/monthly/${employeeId}`, { params: { month } }),

    /** GET /api/attendance/today/:employeeId */
    getToday: (employeeId) => api.get(`/attendance/today/${employeeId}`),

    /**
     * GET /api/attendance/summary?employeeId=X&from=YYYY-MM-DD&to=YYYY-MM-DD
     */
    getSummary: (params) => api.get('/attendance/summary', { params }),
};

export default attendanceService;
