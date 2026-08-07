package com.acme.salary.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Fixed exchange-rate table, not a live FX API - see REQUIREMENTS.md's
 * "deliberately out of scope" and ARCHITECTURE.md's data model notes. Used by
 * both the salary-history creation endpoint and the seed script, so the two
 * paths always agree on how a local-currency amount becomes its USD snapshot.
 */
@Service
public class CurrencyConversionService {

    // Approximate rates, local currency unit -> USD. Fixed on purpose: a
    // snapshot, not a live feed. Revisit periodically, not on every request.
    private static final Map<String, BigDecimal> RATES_TO_USD = Map.ofEntries(
            Map.entry("USD", BigDecimal.ONE),
            Map.entry("GBP", new BigDecimal("1.27")),
            Map.entry("EUR", new BigDecimal("1.08")),
            Map.entry("INR", new BigDecimal("0.012")),
            Map.entry("CAD", new BigDecimal("0.74")),
            Map.entry("AUD", new BigDecimal("0.66")),
            Map.entry("SGD", new BigDecimal("0.74")),
            Map.entry("JPY", new BigDecimal("0.0067")),
            Map.entry("BRL", new BigDecimal("0.17")));

    public BigDecimal toUsd(BigDecimal amount, String currencyCode) {
        return amount.multiply(rateToUsd(currencyCode)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * The inverse of toUsd - local-currency amount for a USD target. Divides
     * by the full-precision rate directly rather than inverting an
     * already-rounded toUsd(1, currency) result: for a low-value currency
     * like JPY (rate 0.0067), rounding that to 2 decimals first gives 0.01,
     * a 33%+ error that would compound into every converted amount.
     */
    public BigDecimal fromUsd(BigDecimal usdAmount, String currencyCode) {
        return usdAmount.divide(rateToUsd(currencyCode), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal rateToUsd(String currencyCode) {
        BigDecimal rate = RATES_TO_USD.get(currencyCode);
        if (rate == null) {
            throw new IllegalArgumentException("No exchange rate configured for currency: " + currencyCode);
        }
        return rate;
    }

    public boolean isSupported(String currencyCode) {
        return RATES_TO_USD.containsKey(currencyCode);
    }

    public java.util.Set<String> supportedCurrencies() {
        return RATES_TO_USD.keySet();
    }
}
