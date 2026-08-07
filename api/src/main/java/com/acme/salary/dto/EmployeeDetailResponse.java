package com.acme.salary.dto;

import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import com.acme.salary.entity.SalaryRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Richer than EmployeeResponse (the list-row shape) - full detail for a single employee. */
public record EmployeeDetailResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String department,
        String jobTitle,
        String level,
        String country,
        String currencyCode,
        EmploymentStatus employmentStatus,
        LocalDate hireDate,
        Instant createdAt,
        Instant updatedAt,
        BigDecimal currentSalaryAmount,
        String currentSalaryCurrency
) {
    public static EmployeeDetailResponse from(Employee employee, SalaryRecord latestSalary) {
        return new EmployeeDetailResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getJobTitle(),
                employee.getLevel(),
                employee.getCountry(),
                employee.getCurrencyCode(),
                employee.getEmploymentStatus(),
                employee.getHireDate(),
                employee.getCreatedAt(),
                employee.getUpdatedAt(),
                latestSalary == null ? null : latestSalary.getAmount(),
                latestSalary == null ? null : latestSalary.getCurrencyCode());
    }
}
