package com.acme.salary.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalaryRecordCreateRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive") BigDecimal amount,
        @NotBlank(message = "Currency code is required") @Size(min = 3, max = 3) String currencyCode,
        @NotNull(message = "Effective date is required") LocalDate effectiveDate,
        @NotBlank(message = "Reason is required") @Size(max = 200) String reason
) {
}
