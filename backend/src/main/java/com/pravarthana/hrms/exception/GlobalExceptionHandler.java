package com.pravarthana.hrms.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler — replaces Spring's whitebox error JSON.
 *
 * Every unhandled exception is caught here and converted to a
 * standardised {@link ApiError} response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        // ── ResponseStatusException (thrown by services) ──────────────────────────
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ApiError> handleResponseStatus(
                        ResponseStatusException ex, HttpServletRequest req) {
                int code = ex.getStatusCode().value();
                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(new ApiError(
                                                LocalDateTime.now(),
                                                code,
                                                ex.getStatusCode().toString(),
                                                ex.getReason(),
                                                req.getRequestURI()));
        }

        // ── @Valid / @Validated failures ───────────────────────────────────────────
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidation(
                        MethodArgumentNotValidException ex, HttpServletRequest req) {
                String message = ex.getBindingResult().getFieldErrors().stream()
                                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                                .collect(Collectors.joining(", "));
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(new ApiError(
                                                LocalDateTime.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Validation Failed",
                                                message,
                                                req.getRequestURI()));
        }

        // ── ConstraintViolationException ───────────────────────────────────────────
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiError> handleConstraintViolation(
                        ConstraintViolationException ex, HttpServletRequest req) {
                String message = ex.getConstraintViolations().stream()
                                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                                .collect(Collectors.joining(", "));
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(new ApiError(
                                                LocalDateTime.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Bad Request",
                                                message,
                                                req.getRequestURI()));
        }

        // ── IllegalArgumentException ───────────────────────────────────────────────
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiError> handleIllegalArgument(
                        IllegalArgumentException ex, HttpServletRequest req) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(new ApiError(
                                                LocalDateTime.now(),
                                                HttpStatus.CONFLICT.value(),
                                                "Conflict",
                                                ex.getMessage(),
                                                req.getRequestURI()));
        }

        // ── AccessDeniedException (403) ────────────────────────────────────────────
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDenied(
                        AccessDeniedException ex, HttpServletRequest req) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(new ApiError(
                                                LocalDateTime.now(),
                                                HttpStatus.FORBIDDEN.value(),
                                                "Forbidden",
                                                "You do not have permission to access this resource.",
                                                req.getRequestURI()));
        }

        // ── Catch-all ──────────────────────────────────────────────────────────────
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest req) {
                // Log the full stacktrace so it appears in backend console
                ex.printStackTrace();
                // Build a human-readable cause chain so the frontend sees the real error
                StringBuilder msg = new StringBuilder(ex.getClass().getSimpleName());
                msg.append(": ").append(ex.getMessage());
                Throwable cause = ex.getCause();
                if (cause != null) {
                        msg.append(" | caused by: ").append(cause.getClass().getSimpleName());
                        msg.append(": ").append(cause.getMessage());
                        if (cause.getCause() != null) {
                                msg.append(" | root: ").append(cause.getCause().getMessage());
                        }
                }
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ApiError(
                                                LocalDateTime.now(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "Internal Server Error",
                                                msg.toString(),
                                                req.getRequestURI()));
        }
}
