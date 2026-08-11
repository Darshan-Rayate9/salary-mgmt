package com.acme.salary.dto;

import com.acme.salary.entity.EmploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * JPQL constructor-expression target for EmployeeRepository's summary queries.
 * A plain class (not a record) - Hibernate's "SELECT new ..." projection has
 * supported this shape reliably across versions, which matters here since
 * this couldn't be compiler-verified while scaffolding (see README).
 *
 * {@code @AllArgsConstructor} generates the constructor in field-declaration
 * order, so that order must stay aligned with the {@code SELECT new
 * EmployeeSummary(...)} column order in EmployeeRepository.
 */
@Getter
@AllArgsConstructor
public class EmployeeSummary {

    private final Long id;
    private final String employeeCode;
    private final String firstName;
    private final String lastName;
    private final String department;
    private final String jobTitle;
    private final String level;
    private final String country;
    private final EmploymentStatus employmentStatus;
    private final BigDecimal currentSalaryAmount;
    private final String currentSalaryCurrency;
}
