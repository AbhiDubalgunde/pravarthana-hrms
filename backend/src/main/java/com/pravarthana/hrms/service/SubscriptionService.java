package com.pravarthana.hrms.service;

import com.pravarthana.hrms.entity.Company;
import com.pravarthana.hrms.repository.CompanyRepository;
import com.pravarthana.hrms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * SubscriptionService — enforces SaaS plan limits.
 *
 * Plan limits:
 *   FREE  → max 10 employees
 *   BASIC → max 50 employees
 *   PRO   → unlimited (-1)
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final CompanyRepository  companyRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Checks if the company can add another employee given their subscription plan.
     *
     * @param companyId the tenant's company ID
     * @throws ResponseStatusException 402 Payment Required if limit is reached
     */
    public void enforceEmployeeLimit(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Company not found: " + companyId));

        int limit = resolveLimit(company.getSubscriptionPlan());
        if (limit == -1) return; // unlimited (PRO)

        long currentCount = employeeRepository.countByCompanyId(companyId);
        if (currentCount >= limit) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    String.format(
                        "Employee limit of %d reached for your '%s' plan. " +
                        "Please upgrade your subscription to add more employees.",
                        limit, company.getSubscriptionPlan()));
        }
    }

    /**
     * Resolves the max employee count for a given plan.
     *
     * @return limit, or -1 if unlimited
     */
    private int resolveLimit(String plan) {
        if (plan == null) return 10;
        return switch (plan.toUpperCase()) {
            case "PRO"   -> -1;   // unlimited
            case "BASIC" -> 50;
            default      -> 10;   // FREE and unknown
        };
    }
}
