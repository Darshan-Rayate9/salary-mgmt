package com.acme.salary.dto;

import com.acme.salary.entity.EmploymentStatus;

import java.math.BigDecimal;

/**
 * JPQL constructor-expression target for EmployeeRepository's summary queries.
 * A plain class (not a record) - Hibernate's "SELECT new ..." projection has
 * supported this shape reliably across versions, which matters here since
 * this couldn't be compiler-verified while scaffolding (see README).
 */
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

    public EmployeeSummary(
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
            String currentSalaryCurrency) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.jobTitle = jobTitle;
        this.level = level;
        this.country = country;
        this.employmentStatus = employmentStatus;
        this.currentSalaryAmount = currentSalaryAmount;
        this.currentSalaryCurrency = currentSalaryCurrency;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDepartment() {
        return department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getLevel() {
        return level;
    }

    public String getCountry() {
        return country;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public BigDecimal getCurrentSalaryAmount() {
        return currentSalaryAmount;
    }

    public String getCurrentSalaryCurrency() {
        return currentSalaryCurrency;
    }
}
