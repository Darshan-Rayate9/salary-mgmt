package com.acme.salary.support;

import com.acme.salary.entity.Employee;
import com.acme.salary.entity.SalaryRecord;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Test-data builder (Object Mother) for {@link SalaryRecord}. The USD equivalent
 * defaults to the local amount, so tests that don't care about conversion can
 * ignore it; the currency-maths itself is covered by CurrencyConversionServiceTest.
 */
public final class SalaryRecordBuilder {

    private Long id;
    private Employee employee;
    private BigDecimal amount = new BigDecimal("102000.00");
    private String currencyCode = "GBP";
    private BigDecimal usdEquivalent;
    private LocalDate effectiveDate = LocalDate.of(2024, 6, 1);
    private String reason = "Merit increase";

    private SalaryRecordBuilder() {
    }

    public static SalaryRecordBuilder aSalaryRecord() {
        return new SalaryRecordBuilder();
    }

    public SalaryRecordBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public SalaryRecordBuilder forEmployee(Employee employee) {
        this.employee = employee;
        return this;
    }

    public SalaryRecordBuilder withAmount(String amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    public SalaryRecordBuilder withCurrency(String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    public SalaryRecordBuilder withUsdEquivalent(String usdEquivalent) {
        this.usdEquivalent = new BigDecimal(usdEquivalent);
        return this;
    }

    public SalaryRecordBuilder effectiveOn(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
        return this;
    }

    public SalaryRecordBuilder because(String reason) {
        this.reason = reason;
        return this;
    }

    public SalaryRecord build() {
        SalaryRecord record = new SalaryRecord();
        record.setId(id);
        record.setEmployee(employee);
        record.setAmount(amount);
        record.setCurrencyCode(currencyCode);
        record.setUsdEquivalent(usdEquivalent != null ? usdEquivalent : amount);
        record.setEffectiveDate(effectiveDate);
        record.setReason(reason);
        return record;
    }
}
