package com.pravarthana.hrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application entry point.
 *
 * @EnableAsync — required for AuditLogService.log() which uses @Async
 * to write audit records without blocking the main request thread.
 */
@SpringBootApplication
@EnableAsync
public class HrmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsApplication.class, args);
    }
}
