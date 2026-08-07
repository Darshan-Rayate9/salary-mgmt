package com.acme.salary.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** employeeCode is intentionally absent - it's immutable once assigned (see Employee entity). */
public record EmployeeUpdateRequest(
        @NotBlank(message = "First name is required") @Size(max = 100) String firstName,
        @NotBlank(message = "Last name is required") @Size(max = 100) String lastName,
        @NotBlank(message = "Email is required") @Email @Size(max = 255) String email,
        @NotBlank(message = "Department is required") @Size(max = 100) String department,
        @NotBlank(message = "Job title is required") @Size(max = 100) String jobTitle,
        @NotBlank(message = "Level is required") @Size(max = 20) String level,
        @NotBlank(message = "Country is required") @Size(max = 100) String country,
        @NotBlank(message = "Currency code is required") @Size(min = 3, max = 3) String currencyCode,
        @NotNull(message = "Hire date is required") LocalDate hireDate
) {
}
