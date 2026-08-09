# ACME Salary Management System

Web-based salary management for ACME's HR Manager. See [REQUIREMENTS.md](REQUIREMENTS.md)
for scope (what's in, what's deliberately left out, and why) and
[ARCHITECTURE.md](ARCHITECTURE.md) for the technical design and trade-offs.

## Live demo

**URL:** https://acme-salary-management-2j30.onrender.com

Log in as **`hr.manager`** / **`Admin@123`**.

> Hosted on Render's free tier — the first request after a period of inactivity
> may take ~30–60s while the instance wakes up. The database is seeded with
> 10,000 employees on boot.

## Stack

Spring Boot 3 (Java 21) + Angular (Angular Material), SQLite, one Docker image.
Auth is HTTP Basic with a single HR Manager account. Full rationale and
trade-offs in ARCHITECTURE.md.

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Node.js 20+ and npm
- (Optional, for the container build) Docker

## Running locally

### Backend

    cd api
    mvn spring-boot:run

Starts on http://localhost:8080. On first boot it runs the Flyway migrations
and seeds one HR Manager login:

- username: `hr.manager` (override with `SEED_ADMIN_USERNAME`)
- password: `ChangeMe123!` (override with `SEED_ADMIN_PASSWORD`)

Change the password via env var in any environment beyond local dev - never
commit a real one.

### Frontend

    cd web
    npm install
    npm start

Starts on http://localhost:4200 and proxies `/api` to the backend on `:8080`
(see `web/proxy.conf.json`).

## Tests

    cd api && mvn test
    cd web && npm test

## Current state

Full-stack and complete. Every feature from REQUIREMENTS.md is built, tested,
and verified end to end against the real 10,000-employee dataset.

**Backend** (Spring Boot, 48 tests):
- HTTP Basic auth with a single in-memory HR Manager account
- Employee directory: paginated list with server-side search (name/code) and
  filters (department, country, level, status)
- Full employee CRUD, with soft-delete (terminate, keep salary history for audit)
- Salary history sub-resource (append-only; current salary is the latest record)
- Compensation analytics: org summary, breakdowns by department/country/level
  (with DB-computed medians), and salary distribution
- Idempotent 10,000-employee seed script (~450ms on boot)

**Frontend** (Angular + Material, 18 tests):
- Login, employee directory with live search/filter
- Employee detail with salary history and an "add raise" dialog
- Create/edit/delete employee forms
- Compensation dashboard: stat cards plus bar charts (ng2-charts) for pay by
  department/level/country and salary distribution

### Verified

Both suites pass (`mvn test` 48/48, `npm test` 18/18), the backend was driven
end to end with `curl` against the running app on the seeded dataset (CRUD,
salary history, search/filter, all analytics endpoints, and auth: 401 without
credentials, 200 with), and the whole UI was walked through in a real browser:
login, directory, employee detail + salary history, and the analytics dashboard
with live charts, all against the real backend.

Real bugs were found and fixed by actually running it at each stage.
Highlights: `hibernate-community-dialects`
version drift; `ddl-auto: validate` vs. SQLite type affinity; a missing
`AuthenticationEntryPoint` (403 instead of 401); SQLite's driver mis-parsing
Hibernate's `Instant`/`LocalDate` output (explicit `AttributeConverter`s); the
`?limit=` param name not matching Spring's default; an unreliable Spring Data
count-query derivation; the catch-all exception handler swallowing framework
405/400/404s into 500s; a missing `<base href>` breaking deep links; and a couple
of TypeScript strict field-initialization-order fixes (switched to `inject()`).

## Building the container

    docker build -t acme-salary-management .
    docker run -p 8080:8080 -v $(pwd)/data:/app/data acme-salary-management

Serves the built Angular app and the API from a single process on `:8080`.
