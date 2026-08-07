package com.acme.salary.service;

import java.time.LocalDate;

/** Input row for EmployeeBulkWriter.insertEmployees - not the API's EmployeeCreateRequest, which has no status field. */
public record NewEmployeeRow(
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String department,
        String jobTitle,
        String level,
        String country,
        String currencyCode,
        String employmentStatus,
        LocalDate hireDate
) {
}
