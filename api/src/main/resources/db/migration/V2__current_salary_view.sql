-- Resolves each employee's current salary (latest SalaryRecord by
-- effectiveDate, id DESC as a deterministic tiebreaker) once, in the
-- database, so every analytics query built on top of this view pushes its
-- aggregation to SQLite rather than pulling rows into the JVM to reduce
-- them (see ARCHITECTURE.md's "push aggregation to the database" principle).
CREATE VIEW current_salary AS
SELECT
    e.id AS employee_id,
    e.department,
    e.country,
    e.level,
    e.employment_status,
    sr.amount,
    sr.currency_code,
    sr.usd_equivalent,
    sr.effective_date
FROM employees e
LEFT JOIN salary_records sr ON sr.id = (
    SELECT sr2.id FROM salary_records sr2
    WHERE sr2.employee_id = e.id
    ORDER BY sr2.effective_date DESC, sr2.id DESC
    LIMIT 1
);
