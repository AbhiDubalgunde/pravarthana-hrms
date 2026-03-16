'use client';

import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

/**
 * ProtectedRoute — wraps a page or layout to enforce authentication.
 *
 * Usage:
 * <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'HR']}>
 *   <SomePage />
 * </ProtectedRoute>
 *
 * If no allowedRoles are provided, any authenticated user can access.
 */
export default function ProtectedRoute({ children, allowedRoles = [] }) {
    const { user, token } = useAuth();
    const router = useRouter();

    useEffect(() => {
        // Not authenticated → redirect to login
        if (!token || !user) {
            router.replace('/login');
            return;
        }

        // Role check if allowedRoles is specified
        if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
            router.replace('/dashboard'); // redirect to home, not login
        }
    }, [token, user, allowedRoles, router]);

    // Don't render children until authentication is confirmed
    if (!token || !user) return null;
    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) return null;

    return <>{children}</>;
}
