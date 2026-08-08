package com.acme.salary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Append-only: salary changes are never edited or overwritten, only added.
 * An employee's "current salary" is derived as the record with the latest
 * effectiveDate - see EmployeeRepository for how that is resolved in bulk.
 */
@Entity
@Table(
        name = "salary_records",
        indexes = {
                @Index(name = "idx_salary_records_employee_effective_date", columnList = "employee_id, effective_date")
        }
)
@Getter
@Setter
public class SalaryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_salary_records_employee"))
    private Employee employee;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    /** Snapshot at record-creation time from a fixed rate table - never a live FX call. */
    @Column(name = "usd_equivalent", nullable = false, precision = 14, scale = 2)
    private BigDecimal usdEquivalent;

    @Convert(converter = LocalDateStringConverter.class)
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false, length = 200)
    private String reason;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE) // managed by @PrePersist, never set from outside
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
