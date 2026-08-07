package com.acme.salary.dto;

import java.math.BigDecimal;

/** One row of the by-department / by-country / by-level breakdowns - same shape, different grouping column. */
public record GroupAggregateResponse(
        String groupName,
        long headcount,
        BigDecimal avgSalaryUsd,
        BigDecimal medianSalaryUsd,
        BigDecimal minSalaryUsd,
        BigDecimal maxSalaryUsd,
        BigDecimal totalPayrollCostUsd
) {
}
