package com.acme.salary.service;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Bulk-write path for the employee seed script - "insert a chunk of employees
 * (and their salary records) as one transaction." Kept as its own component so
 * any future bulk-import path can reuse it. Plain JdbcTemplate, not JPA:
 * bypassing the persistence-context/dirty-checking overhead matters at these
 * row counts (10k+ employees for the seed script).
 */
@Component
public class EmployeeBulkWriter {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeBulkWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * One row at a time, not JDBC's batch protocol - IDENTITY-generated keys
     * have to be read back individually per statement, so the win here is
     * one transaction per chunk (bounded commit cost), not batched wire
     * protocol. Returns generated ids in the same order as the input.
     */
    @Transactional
    public List<Long> insertEmployees(List<NewEmployeeRow> rows) {
        List<Long> ids = new ArrayList<>(rows.size());
        String now = Instant.now().toString();

        for (NewEmployeeRow row : rows) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        INSERT INTO employees (employee_code, first_name, last_name, email, department,
                            job_title, level, country, currency_code, employment_status, hire_date,
                            created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, row.employeeCode());
                ps.setString(2, row.firstName());
                ps.setString(3, row.lastName());
                ps.setString(4, row.email());
                ps.setString(5, row.department());
                ps.setString(6, row.jobTitle());
                ps.setString(7, row.level());
                ps.setString(8, row.country());
                ps.setString(9, row.currencyCode());
                ps.setString(10, row.employmentStatus());
                ps.setString(11, row.hireDate().toString());
                ps.setString(12, now);
                ps.setString(13, now);
                return ps;
            }, keyHolder);
            ids.add(keyHolder.getKey().longValue());
        }

        return ids;
    }

    /** No generated keys needed back here, so this one does use real JDBC batching. */
    @Transactional
    public void insertSalaryRecords(List<NewSalaryRow> rows) {
        if (rows.isEmpty()) {
            return;
        }
        String now = Instant.now().toString();
        jdbcTemplate.batchUpdate("""
                        INSERT INTO salary_records
                            (employee_id, amount, currency_code, usd_equivalent, effective_date, reason, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                rows, rows.size(),
                (ps, row) -> {
                    ps.setLong(1, row.employeeId());
                    ps.setBigDecimal(2, row.amount());
                    ps.setString(3, row.currencyCode());
                    ps.setBigDecimal(4, row.usdEquivalent());
                    ps.setString(5, row.effectiveDate().toString());
                    ps.setString(6, row.reason());
                    ps.setString(7, now);
                });
    }
}
