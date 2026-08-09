# AI-Assisted Development — Prompts & Workflow

This project was built in an **AI-native workflow**: an agentic AI tool was used
across the SDLC (requirements framing, an architecture-review pass, scaffolding,
feature implementation, test generation, debugging, and refactoring), while
engineering judgment stayed human — scope, data model, security posture, and the
decision to *remove* AI-built complexity were mine.

> The prompts below are **representative** of how I directed the AI —
> reconstructed from the resulting commits and code notes — not a verbatim
> transcript. The *examples of AI mistakes I caught* (further down) are concrete
> and traceable to specific comments in the code.

## Tooling

- **Agentic AI:** `[CONFIRM: e.g. Claude Code (Anthropic) / model]`
- **Editor / runtime:** local dev, JDK 21 + Maven, Node 20 + Angular CLI
- **Validation loop for every AI output:** `mvn test`, `npm test`, `curl` against
  the running API, and a real browser walkthrough — nothing was accepted just
  because it compiled.

## Working principle

**AI accelerates; judgment governs.** I used AI to move fast on the mechanical
90% (scaffolding, boilerplate, first-draft queries and tests) and spent my own
effort on the 10% that decides whether the system is correct and maintainable:
what to build, what to *leave out*, how to model salary history, how money is
rounded, and what to cut when the AI over-engineered.

## Workflow, phase by phase

### 1. Requirements & scope (`docs: add one-page requirements document`)
Representative prompt:
> "Draft a one-page requirements doc for salary-management software for a single
> HR-Manager persona over 10k employees. Separate in-scope features from what
> we'll deliberately leave out, and force a reason for every exclusion."

**Human judgment:** I decided the actual exclusions (no live FX, no RBAC, no
approval workflow, no payroll execution) and the reasoning — the AI would happily
have built all of them.

### 2. Architecture & a design-review pass (`docs: harden architecture doc after design review`)
Representative prompt:
> "Challenge this design. For a single-writer 10k-row CRUD+reporting app, is
> SQLite defensible? Is HTTP Basic enough, or am I under-building auth? Where
> would this break first, and what am I over-engineering?"

**Human judgment:** kept SQLite + HTTP Basic *because* the persona is one HR
Manager, and pushed aggregation into SQL rather than Java. The "harden after
review" commit is the AI red-teaming the doc, then me deciding what to accept.

### 3. Scaffolding from the architecture doc (`feat: scaffold … per ARCHITECTURE.md`)
Representative prompt:
> "Scaffold the Spring Boot service to match ARCHITECTURE.md: entities,
> Flyway V1 schema, repository interfaces, controller/service/DTO layering, and
> the global exception handler contract."

### 4. Feature implementation — thin vertical slices
Each feature landed as its own commit (directory API → salary history → analytics
→ seed/security). Representative prompt:
> "Implement the paginated employee directory. Resolve each employee's *current*
> salary (latest record by effective date) for a whole page in **one** query —
> no per-row lazy loading — and return DTOs, never entities."

**Human judgment:** the append-only salary model, the USD-snapshot approach, and
the DTO boundary were design decisions I made and then asked the AI to implement.

### 5. Tests (`test(api): service, repository, and integration tests`)
Representative prompt:
> "Write fast, deterministic tests: Mockito unit tests for the service business
> rules (soft-delete never hard-deletes, duplicate code/email → conflict, blank
> filters → null), `@DataJpaTest` against the real Flyway schema for the custom
> queries, and one `@SpringBootTest` for the create→raise→analytics flow."

> **Honest note:** these were written *alongside/after* the features, not strict
> test-first TDD. I relied heavily on running the app to find defects. In a team
> setting I'd move to test-first for the business rules.

### 6. Debugging by running it (the real payoff)
I ran the app at each stage and fed failures back to the AI to fix. See the next
section — these are the concrete cases.

### 7. Refactoring & scope-down (`refactor: scope down to core; JWT → HTTP Basic`)
Representative prompt:
> "This has grown a JWT auth subsystem and a CSV importer. For a single HR-Manager
> internal tool that's over-built — strip JWT down to stateless HTTP Basic and
> remove CSV import/export; the seed script already covers the data load."

**Human judgment:** the most important AI-related decision on the project was
*removing* code the AI and I had built. Cutting complexity is craftsmanship.

## Where I did **not** delegate to AI

- **Scope** — what to build and (harder) what to leave out.
- **Data model** — append-only `salary_records`, derived current salary, USD
  snapshot for cross-country analytics.
- **Security posture** — single in-memory user, credential in `sessionStorage`
  (not `localStorage`), no `WWW-Authenticate` header in front of the SPA.
- **Money correctness** — `BigDecimal` throughout, and dividing by the
  full-precision rate in `fromUsd` so a low-value currency like JPY doesn't
  accumulate rounding error.
- **The decision to delete AI-built complexity** (JWT, CSV).

## Concrete cases where AI got it wrong (and how I caught it)

Every one of these was surfaced by **running the code**, not by reading it — and
each is documented at the point in the code where it was fixed.

| # | AI-generated code was wrong because… | How I caught it | Fix |
|---|---|---|---|
| 1 | `hibernate-community-dialects` version didn't match the `hibernate-core` pulled in by Spring Boot 3.3.4 | boot failure | pinned the version to match, documented in `pom.xml` |
| 2 | `ddl-auto: validate` fails against a *correct* SQLite schema (driver reports `INTEGER PRIMARY KEY AUTOINCREMENT` as `INTEGER`, Hibernate expects `BIGINT`) | boot failure, confirmed by actually running | dropped to `ddl-auto: none`; documented in `application.yml` |
| 3 | Spring Security returned **403 instead of 401** with no `AuthenticationEntryPoint` | `curl` without credentials | custom entry point returning JSON 401, no `WWW-Authenticate` popup |
| 4 | SQLite driver mis-parsed Hibernate's `Instant`/`LocalDate` output | date columns read back wrong | explicit `AttributeConverter`s writing plain ISO-8601 text |
| 5 | `?limit=` was silently ignored — Spring's default page-size param is `size` | endpoint always returned 20 | set `spring.data.web.pageable.size-parameter: limit` |
| 6 | Spring Data's auto-derived **count query** broke on the `SELECT new …(…)` projection with nested subqueries | integration test: created rows, listed them, got an empty page | explicit `countQuery` on the `@Query` |
| 7 | The catch-all exception handler swallowed framework `405/400/404` into bare `500`s | GET on a POST-only URL returned 500 | explicit handlers for `HttpRequestMethodNotSupported`, `MethodArgumentTypeMismatch`, `NoResourceFound`, malformed body |
| 8 | Missing `<base href>` broke Angular deep links | reload on a deep route 404'd | added `<base href="/">` |
| 9 | TypeScript strict field-initialization-order errors | build failure, found by actually building | switched to `inject()` |

## How I validated AI-generated code

1. **Automated:** `mvn test` (48) and `npm test` (18) — fast, deterministic, no
   network/filesystem/wall-clock dependence.
2. **API:** drove every endpoint with `curl` on the seeded 10k dataset (auth
   401/200, CRUD, salary history, all analytics).
3. **UI:** walked the whole app in a real browser against the live backend.
4. **Targeted review** for the risks AI tends to miss: entities leaking through
   the API boundary, N+1 on the directory page, and SQL-aggregate correctness
   (the median and distribution queries).

## What I'd do differently in a production team

- **Test-first** for business rules, so the AI implements against a red test.
- **Commit the prompts** alongside the code so review sees the intent.
- Put AI in **CI** for a first-pass review (code smells, obvious bugs) — while
  keeping a human accountable for correctness, security, and maintainability.
- Pair on the *prompt* for anything security- or money-related.
