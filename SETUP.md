# Setup & Run Guide

How to run the ACME Salary Management System locally. Two ways: run the
backend and frontend separately (best for development), or build the single
Docker container (backend + frontend in one image).

## Prerequisites

| Tool | Version | Check with |
|---|---|---|
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js + npm | 20+ | `node -v` / `npm -v` |
| Docker (optional) | any recent | `docker -v` |

> On this machine the toolchain is installed via Homebrew under
> `/opt/homebrew`. If a fresh terminal can't find `java`/`mvn`/`node`, ensure
> your shell loads Homebrew's path, e.g.:
> ```bash
> eval "$(/opt/homebrew/bin/brew shellenv)"
> export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
> ```
> Maven must run under Java 21 specifically — confirm with `mvn -version`
> (it prints the Java version it's using). If it shows a different JDK, set
> `export JAVA_HOME="/opt/homebrew/opt/openjdk@21"`.

---

## Option A: Run locally (two processes)

### 1. Backend (port 8080)

```bash
cd api
mvn spring-boot:run
```

On first boot it runs the database migrations and seeds:
- **10,000 employees** with salary history (takes ~0.5s)
- One **HR Manager** login

The API is then available at `http://localhost:8080`. Verify:

```bash
curl http://localhost:8080/actuator/health
```

### 2. Frontend (port 4200)

In a **second terminal**:

```bash
cd web
npm install        # first time only
npm start
```

Open **http://localhost:4200** in your browser. The dev server proxies
`/api` calls to the backend on `:8080` (see `web/proxy.conf.json`), so
both must be running.

### 3. Log in

- **Username:** `hr.manager`
- **Password:** `ChangeMe123!`

From there: browse/search/filter the directory, open an employee to see and
add salary records, create/edit/delete employees, and view the compensation
dashboard.

---

## Option B: Run as a single Docker container

Builds the Angular app, embeds it into the Spring Boot jar, and serves both
the UI and the API from one process on port 8080.

```bash
docker build -t acme-salary-management .
docker run -p 8080:8080 -v "$(pwd)/data:/app/data" acme-salary-management
```

Then open **http://localhost:8080** (UI and API are on the same origin here —
no separate frontend server, no `:4200`).

---

## Running the tests

Both suites are fast and deterministic — no network, filesystem, or wall-clock
dependencies — and run offline once dependencies are cached.

### Backend (JUnit 5 + Mockito) — 48 tests

```bash
cd api
mvn test
```

Runs the full suite: service-layer unit tests (mocked repositories),
`@DataJpaTest` repository tests against the **real Flyway-managed SQLite schema**
(not H2), and `@SpringBootTest` MockMvc integration tests. The 10k seed is
skipped under the `test` profile, so the suite stays fast (typically < 30s).

Run a single test class, or a single method:

```bash
mvn -Dtest=EmployeeServiceTest test
mvn -Dtest=EmployeeServiceTest#createEmployee_valid_savesWithActiveStatusAndNoSalaryYet test
```

> Maven must run under **Java 21** (see Prerequisites). If `mvn -version` shows a
> different JDK, set it explicitly:
> ```bash
> export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
> ```

### Frontend (Jasmine + Karma) — 18 tests

```bash
cd web
npm install                                    # first time only
npm test                                        # interactive watch mode (opens Chrome)
```

For a single, non-interactive run (e.g. CI):

```bash
npm test -- --watch=false --browsers=ChromeHeadless
```

Frontend tests cover the auth service, HTTP interceptor, route guard, and the
employee-list component. They need **Chrome/Chromium** installed for the runner.

---

## Configuration & notes

- **Database:** SQLite, stored at `api/data/salary.db` (created on first
  run, gitignored). Delete it to reset; the seed re-runs on the next boot.
- **Seeded data regenerates on a fresh DB** — the seeder is idempotent and
  only runs when the `employee` table is empty, so restarting with an existing
  DB keeps your changes.
- **Auth is HTTP Basic**; the encoded credential is kept in `sessionStorage`
  (not `localStorage`), so a page refresh keeps you logged in, but closing the
  tab logs you out. Chosen over localStorage so a reversible Basic credential is
  never persisted to disk long-term (see ARCHITECTURE.md).
- **Overridable via environment variables:**

  | Variable | Default | Purpose |
  |---|---|---|
  | `SEED_ADMIN_USERNAME` | `hr.manager` | HR Manager login |
  | `SEED_ADMIN_PASSWORD` | `ChangeMe123!` | HR Manager password (change outside local dev) |
  | `SEED_EMPLOYEE_COUNT` | `10000` | Number of employees to seed |
  | `SALARY_DB_PATH` | `./data/salary.db` | SQLite file location |

  Example — seed a smaller dataset with a custom password:
  ```bash
  cd api
  SEED_EMPLOYEE_COUNT=200 SEED_ADMIN_PASSWORD='S3cret!' mvn spring-boot:run
  ```

## Ports

| Service | Port | URL |
|---|---|---|
| Backend API | 8080 | http://localhost:8080/api/v1 |
| Frontend (dev) | 4200 | http://localhost:4200 |
| Docker (combined) | 8080 | http://localhost:8080 |
