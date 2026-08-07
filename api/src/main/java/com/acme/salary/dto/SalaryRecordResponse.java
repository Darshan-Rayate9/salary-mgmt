package com.acme.salary.dto;

import com.acme.salary.entity.SalaryRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SalaryRecordResponse(
        Long id,
        BigDecimal amount,
        String currencyCode,
        BigDecimal usdEquivalent,
        LocalDate effectiveDate,
        String reason,
        Instant createdAt
) {
    public static SalaryRecordResponse from(SalaryRecord record) {
        return new SalaryRecordResponse(
                record.getId(),
                record.getAmount(),
                record.getCurrencyCode(),
                record.getUsdEquivalent(),
                record.getEffectiveDate(),
                record.getReason(),
                record.getCreatedAt());
    }
}
