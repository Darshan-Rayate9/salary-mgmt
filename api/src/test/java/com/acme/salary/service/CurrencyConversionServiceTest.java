package com.acme.salary.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyConversionServiceTest {

    private final CurrencyConversionService service = new CurrencyConversionService();

    @Test
    void toUsd_usd_isIdentity() {
        assertThat(service.toUsd(new BigDecimal("1000.00"), "USD")).isEqualByComparingTo("1000.00");
    }

    @Test
    void toUsd_appliesFixedRateAndRoundsToTwoDecimals() {
        assertThat(service.toUsd(new BigDecimal("100000.00"), "GBP")).isEqualByComparingTo("127000.00");
    }

    @Test
    void toUsd_unsupportedCurrency_throws() {
        assertThatThrownBy(() -> service.toUsd(BigDecimal.TEN, "XXX"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isSupported_reflectsTheRateTable() {
        assertThat(service.isSupported("USD")).isTrue();
        assertThat(service.isSupported("XXX")).isFalse();
    }

    @Test
    void fromUsd_isTheInverseOfToUsd() {
        BigDecimal localAmount = service.fromUsd(new BigDecimal("127000.00"), "GBP");

        assertThat(localAmount).isEqualByComparingTo("100000.00");
    }

    @Test
    void fromUsd_lowValueCurrency_doesNotLosePrecisionFromRoundingTheRateFirst() {
        // JPY's rate (0.0067) rounds to 0.01 if you round the rate before dividing -
        // a 33%+ error. fromUsd must divide by the full-precision rate directly.
        BigDecimal localAmount = service.fromUsd(new BigDecimal("10000.00"), "JPY");

        assertThat(localAmount).isEqualByComparingTo("1492537.31");
    }

    @Test
    void fromUsd_unsupportedCurrency_throws() {
        assertThatThrownBy(() -> service.fromUsd(BigDecimal.TEN, "XXX"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
