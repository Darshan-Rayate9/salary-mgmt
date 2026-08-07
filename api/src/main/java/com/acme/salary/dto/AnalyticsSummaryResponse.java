package com.acme.salary.dto;

import java.math.BigDecimal;

public record AnalyticsSummaryResponse(
        long headcount,
        BigDecimal avgSalaryUsd,
        BigDecimal medianSalaryUsd,
        BigDecimal minSalaryUsd,
        BigDecimal maxSalaryUsd,
        BigDecimal totalPayrollCostUsd
) {
}
