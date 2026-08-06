# ACME Salary Management System — Requirements Document

## Goal
Replace ACME's spreadsheet-based salary tracking with a web-based system that lets
the HR Manager view, manage, and analyze compensation data for ~10,000 employees
across multiple countries, and answer questions about how the org pays people
(e.g. pay distribution by department, country, and level).

## Primary User
HR Manager — a single, org-wide role. This is not a self-serve tool for employees
or line managers.

## Scope & Features (In)
1. **Employee directory** — list, search, filter (department, country, level,
   employment status), sort, paginate. Must stay responsive at 10k rows
   (server-side filtering/pagination, not client-side loading of all records).
2. **Employee profile** — view/edit core fields (name, id, department, job
   title/level, country, currency, employment status, hire date) and current salary.
3. **Salary history, not overwrite** — every salary change is a dated record
   (effective date + reason, e.g. "merit increase", "promotion", "market
   adjustment"), so the system keeps an auditable trail per employee instead of
   silently overwriting a single number.
4. **Compensation analytics dashboard** — the direct answer to "how do we pay
   people": avg/median/min/max salary sliced by department, country, and level;
   salary distribution; headcount and payroll cost by country/department.
5. **Basic authentication** — a single HTTP Basic login gating the app, since
   this is now a live web app rather than a local spreadsheet.
6. **Seed script** — generates 10,000 realistic employees (varied country,
   currency, department, level, salary, hire date) for demo and testing.

## Deliberately Out of Scope
- **Bulk CSV import/export** — considered as the Excel on-ramp/off-ramp, but the
  seed script already covers the 10k data load this exercise needs, and a
  production-grade importer (encoding edge cases, partial-failure UX, dedupe
  rules) is more surface area than the core task warrants. Left out to keep the
  scope matched to the problem.
- **Outlier / anomaly detection** — a "flag anyone far from their peer median"
  view is a nice-to-have, not part of the core "how do we pay people" question:
  the avg/median/min/max breakdowns by department, country, and level already
  answer that. Cut to avoid gold-plating the analytics.
- **Payroll execution, bank disbursement, tax/statutory compliance** — this is a
  system of record and insight tool, not a payroll processor. Real money
  movement is a regulated, country-specific problem with legal/financial
  consequences that can't be responsibly validated in this exercise.
- **Live multi-currency conversion** — each employee's salary is stored in local
  currency plus a fixed USD-equivalent snapshot (set at creation/seed time) so
  cross-country analytics are still possible. No live FX API: correctness of a
  live-rate feed isn't the thing being tested here, and it adds an external
  dependency for no validated requirement.
- **Multiple roles / granular RBAC** (country HR, manager or employee self-serve,
  etc.) — the stated persona is a single org-wide HR Manager; building a
  permission matrix nobody asked for is speculative scope.
- **Approval workflows / multi-step sign-off** for salary changes — not in the
  problem statement; the HR Manager is trusted to edit directly, which keeps the
  data model and UI simple.
- **Integrations with external HRIS/payroll systems** (Workday, ADP, etc.) — per
  the prompt, ACME currently only has Excel; there is nothing to integrate with.
- **Notifications/email, org-chart visualization, performance/review data,
  benefits & equity comp** — adjacent HR domains that would dilute focus from
  the stated problem of salary data management and pay insight.
- **Mobile-first/responsive design** — constraints specify web-based software;
  desktop-first is the right default for an HR back-office tool, though the UI
  won't be deliberately broken on smaller screens.
- **Multi-tenant support** — single organization (ACME), single deployment.

## Non-Functional Notes
- Must comfortably handle 10,000 employee records: indexed queries, server-side
  pagination/filtering, and aggregate queries computed in the database rather
  than in application memory.
- Because this is compensation data, correctness and auditability (who changed
  a salary, when, and why) matter more than raw feature count.
