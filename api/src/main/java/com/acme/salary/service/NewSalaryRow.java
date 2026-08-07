package com.acme.salary.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NewSalaryRow(
        Long employeeId,
        BigDecimal amount,
        String currencyCode,
        BigDecimal usdEquivalent,
        LocalDate effectiveDate,
        String reason
) {
}
