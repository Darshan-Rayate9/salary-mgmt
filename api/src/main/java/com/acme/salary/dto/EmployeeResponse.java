package com.acme.salary.dto;

import com.acme.salary.entity.EmploymentStatus;

import java.math.BigDecimal;

/**
 * API-facing DTO - controllers never return JPA entities directly (see
 * ARCHITECTURE.md "Entities never leave the service layer"), so nothing
 * persistence-only (e.g. a password hash on another entity, a lazy proxy)
 * can leak into a response body.
 */
public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String department,
        String jobTitle,
        String level,
        String country,
        EmploymentStatus employmentStatus,
        BigDecimal currentSalaryAmount,
        String currentSalaryCurrency
) {
    public static EmployeeResponse from(EmployeeSummary summary) {
        return new EmployeeResponse(
                summary.getId(),
                summary.getEmployeeCode(),
                summary.getFirstName(),
                summary.getLastName(),
                summary.getDepartment(),
                summary.getJobTitle(),
                summary.getLevel(),
                summary.getCountry(),
                summary.getEmploymentStatus(),
                summary.getCurrentSalaryAmount(),
                summary.getCurrentSalaryCurrency()
        );
    }
}
