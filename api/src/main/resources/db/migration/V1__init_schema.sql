-- Initial schema. Flyway-managed (not Hibernate ddl-auto) so schema changes
-- stay explicit, reviewable, and reversible - see ARCHITECTURE.md.

CREATE TABLE employees (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_code      VARCHAR(20)  NOT NULL,
    first_name         VARCHAR(100) NOT NULL,
    last_name          VARCHAR(100) NOT NULL,
    email              VARCHAR(255) NOT NULL,
    department         VARCHAR(100) NOT NULL,
    job_title          VARCHAR(100) NOT NULL,
    level              VARCHAR(20)  NOT NULL,
    country            VARCHAR(100) NOT NULL,
    currency_code      VARCHAR(3)   NOT NULL,
    employment_status  VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    hire_date          DATE         NOT NULL,
    created_at         TEXT         NOT NULL,
    updated_at         TEXT         NOT NULL
);

CREATE UNIQUE INDEX uk_employees_employee_code ON employees (employee_code);
CREATE UNIQUE INDEX uk_employees_email ON employees (email);
CREATE INDEX idx_employees_department ON employees (department);
CREATE INDEX idx_employees_country ON employees (country);
CREATE INDEX idx_employees_level ON employees (level);
CREATE INDEX idx_employees_status ON employees (employment_status);

CREATE TABLE salary_records (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id     INTEGER        NOT NULL REFERENCES employees (id),
    amount          DECIMAL(14,2)  NOT NULL,
    currency_code   VARCHAR(3)     NOT NULL,
    usd_equivalent  DECIMAL(14,2)  NOT NULL,
    effective_date  DATE           NOT NULL,
    reason          VARCHAR(200)   NOT NULL,
    created_at      TEXT           NOT NULL
);

CREATE INDEX idx_salary_records_employee_effective_date ON salary_records (employee_id, effective_date);
