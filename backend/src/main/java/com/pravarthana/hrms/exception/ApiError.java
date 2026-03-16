package com.pravarthana.hrms.exception;

import java.time.LocalDateTime;

/**
 * Standardised API error response body.
 * Replaces Spring's default whitebox error JSON.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
