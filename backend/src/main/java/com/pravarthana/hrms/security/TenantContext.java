package com.pravarthana.hrms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * TenantContext — extracts the current tenant's companyId from the
 * authenticated JWT stored in the Spring SecurityContext.
 *
 * Usage (inside any service):
 * <pre>
 *   Long companyId = TenantContext.getCompanyId();
 * </pre>
 */
@Component
public class TenantContext {

    /**
     * Returns the companyId embedded in the JWT for the currently authenticated user.
     *
     * @throws IllegalStateException if no authentication is present or companyId is missing.
     */
    public static Long getCompanyId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in context.");
        }

        Object details = auth.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object companyId = map.get("companyId");
            if (companyId instanceof Long l) return l;
            if (companyId instanceof Integer i) return i.longValue();
            if (companyId instanceof Number n) return n.longValue();
        }

        // Fall back to 1L (demo company) — acceptable in dev, reject in prod if needed
        return 1L;
    }

    /**
     * Returns the userId of the currently authenticated user.
     */
    public static Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in context.");
        }
        Object details = auth.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object userId = map.get("userId");
            if (userId instanceof Long l) return l;
            if (userId instanceof Integer i) return i.longValue();
            if (userId instanceof Number n) return n.longValue();
        }
        return null;
    }

    /**
     * Returns the role string of the currently authenticated user.
     */
    public static String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse(null);
    }
}
