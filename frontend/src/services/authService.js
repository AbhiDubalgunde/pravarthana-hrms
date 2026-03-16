/**
 * authService.js — Authentication API service layer.
 * Centralises all auth-related API calls.
 */
import api from './api';

export const authService = {
    /**
     * POST /api/auth/login
     * @param {{ email: string, password: string }} credentials
     * @returns {{ token: string, user: object }}
     */
    login: (credentials) => api.post('/auth/login', credentials),

    /**
     * POST /api/auth/register
     * @param {{ email, password, roleId, firstName, lastName }} data
     */
    register: (data) => api.post('/auth/register', data),

    /**
     * POST /api/auth/forgot-password
     * @param {{ email: string }} data
     */
    forgotPassword: (data) => api.post('/auth/forgot-password', data),

    /**
     * POST /api/auth/reset-password
     * @param {{ token: string, newPassword: string }} data
     */
    resetPassword: (data) => api.post('/auth/reset-password', data),
};

export default authService;
