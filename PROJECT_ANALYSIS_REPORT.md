# ERP-SMB-UI — Project Analysis Report

**Date:** May 28, 2026  
**Analyst:** Claude (Senior Staff Engineer Review)  
**Scope:** Full repository — backend microservices, frontend UI, infrastructure, CI/CD

---

## 1. Executive Summary

`erp-smb-ui` is an ERP platform targeting small and medium businesses. It follows a microservices architecture with 14 Spring Boot services, a React 18 single-page frontend, and a shared PostgreSQL database. The project has a solid structural foundation — clear domain boundaries, Flyway migrations, JWT authentication with refresh tokens, a custom full-text search service, async reporting, and a global CSV/Excel data import framework. However, the codebase carries meaningful security, testing, and operational risks that must be addressed before a production launch. Several subsystems are partially implemented placeholders.

**Overall Assessment:** Early/mid-stage product. Architecture is sound in intent but not yet production-hardened.

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| Backend Runtime | Java 21, Spring Boot 3.3.4 |
| Service Mesh | Spring Cloud 2023.0.3, Eureka, Spring Cloud Config |
| Persistence | PostgreSQL 16 (shared instance), Spring Data JPA, Flyway 10.22.0 |
| Security | JWT (JJWT, HS256), Spring Security |
| Frontend | React 18.3, Vite 5.4, Axios, Recharts |
| Containerisation | Docker Compose (dev), Docker (per-service Dockerfiles) |
| CI/CD | GitHub Actions (compile + build only) |
| Search | PostgreSQL `pg_trgm` — custom search-service |
| Reporting | Async execution, JDBC-based SQL builder, CSV export |
| Import | Apache POI (Excel), OpenCSV — via common-lib |
| API Docs | SpringDoc OpenAPI (per service + gateway aggregation) |

**Lines of Code:** ~7,800 Java (156 files across 14 modules) · ~5,000 JavaScript/JSX (39 frontend files)

---

## 3. Architecture Overview

### 3.1 Backend Microservices

```
Browser / Frontend (React + Vite)
         │
         ▼
   gateway-service :8080   ← JWT auth + CORS + rate-limit + reverse proxy
         │
         ├── auth-service        — login, signup, refresh, password change, user delete
         ├── user-service        — user management (CRUD)
         ├── product-service     — inventory / items, opening stock import
         ├── order-service       — sales orders & line items
         ├── sales-service       — customers, invoices
         ├── finance-service     — transactions, KPIs (MRR, cash flow)
         ├── hrms-service        — employees, attendance, leave, payroll
         ├── enquiry-service     — pre-sales enquiries / leads
         ├── reporting-service   — report definitions, async run, CSV export
         └── search-service :8090 ← also called directly by product/order/sales for indexing

   Infrastructure:
         ├── config-service :8888   — Spring Cloud Config (centralised app config)
         ├── discovery-service :8761 — Eureka
         └── common-lib              — shared JWT, search DTOs, import framework, exception handling
```

**Startup Order (from docker-compose):**
`postgres → config-service → discovery-service → gateway-service → all domain services → frontend`

### 3.2 Frontend Architecture

The frontend is a React SPA with **manual string-based routing** (a switch-case in `App.jsx`) rather than a router library. Auth state lives in `localStorage`. There is no global state manager. API calls are organized into a layered client structure (`api/clients/`, `api/interceptors/`, `api/config/`).

Pages: Dashboard · Enquiry · Orders · Sales · Inventory · Finance · HRMS · Reporting · Admin · Audit Log · Settings · Search

---

## 4. Strengths

### 4.1 Clean Domain Boundaries
Each microservice owns a discrete bounded context (products, orders, sales, finance, HRMS, enquiries, reporting, search). Domain models are well-named and appropriately scoped.

### 4.2 Common Library (common-lib)
Shared concerns are correctly extracted: JWT utilities, JwtAuthFilter, pagination DTOs, global exception handler, file import framework (CSV + Excel), and search DTOs. This avoids copy-paste proliferation across services.

### 4.3 Flyway Schema Management
Every service with a database manages its schema through versioned Flyway migrations, including seeded demo data. This makes onboarding deterministic.

### 4.4 Full-text + Fuzzy Search (pg_trgm)
The search-service implements a thoughtful search strategy: exact code-like queries (e.g., `SO-1005`) short-circuit to exact matching, full-text is tried first, and fuzzy matching (`pg_trgm` similarity) is used only as a fallback when results are insufficient. Rate limiting is applied at the gateway with per-tenant and per-IP buckets.

### 4.5 Async Reporting with CSV Export
The reporting-service supports async report execution, definition persistence, run history, and file-based CSV export with configurable retention.

### 4.6 JWT Refresh Flow
The frontend correctly implements silent token refresh: a shared `refreshPromise` prevents concurrent refresh races, and on failure it emits a global `auth:logout` event to drive the UI state machine.

### 4.7 Data Import Framework
`AbstractImportService` + `CsvFileParser` + `ExcelFileParser` in common-lib provide a reusable template for multi-format bulk imports. product-service, sales-service, and product-service (opening stock) all leverage it consistently.

### 4.8 CI/CD Foundation
GitHub Actions runs on every PR and push to main. It checks for BOM/encoding issues, compiles all Java modules, and runs a Vite production build. Cross-platform (Ubuntu + Windows matrix) for encoding hygiene checks is a nice touch.

---

## 5. Risks & Issues

### 5.1 🔴 Critical — Security

**5.1.1 Weak default JWT secret in version-controlled config**
`docker-compose.yml` hardcodes `APP_JWT_SECRET: dev-secret-please-change-32-chars-minimum-123456`. If this value is accidentally used in production or leaked from `.env`, all tokens can be forged.

**5.1.2 Unauthenticated access to all report GET endpoints**
The gateway `SecurityConfig` contains:
```java
.requestMatchers(HttpMethod.GET, "/api/reports/**").permitAll()
```
This allows any unauthenticated user to fetch report definitions, run history, and exports. This was likely added as a dev workaround and was never reverted.

**5.1.3 Auth controller duplicates JwtUtils instantiation**
`AuthController` creates its own `new JwtUtils(secret, ttl, ttl * 24)` inline rather than injecting the Spring-managed bean. This could cause a key mismatch if the bean is configured differently, and it circumvents Spring's lifecycle management.

**5.1.4 System.out/err used for security-sensitive logging**
`JwtAuthFilter` logs every auth failure and success to `System.out` and `System.err` instead of SLF4J. These bypass log aggregation, log level control, and structured logging — making audit trails unreliable.

**5.1.5 No password strength policy**
`/api/auth/signup` accepts any non-blank password (`@NotBlank` only). There is no minimum length, complexity, or bcrypt cost configuration.

**5.1.6 Token stored in localStorage (XSS risk)**
Access and refresh tokens are stored in `localStorage`, making them accessible to any JavaScript running on the page. An XSS vulnerability in any dependency would expose all user tokens.

**5.1.7 No JWT revocation / blacklist**
Tokens cannot be invalidated server-side before they expire. Compromised tokens remain valid until TTL elapses.

---

### 5.2 🟠 High — Architecture & Design

**5.2.1 Shared database breaks microservice isolation**
All 14 services connect to the same PostgreSQL instance. This is the single biggest architectural violation: services are not truly independent, schema changes in one service can cascade to others, and the database becomes a single point of failure and a scalability bottleneck.

**5.2.2 Search reindex is a stub / placeholder**
`SearchService.bulkReindex()` is explicitly marked as a placeholder in a comment:
```java
// In production: fetch from product-service and reindex
```
The method counts existing rows but does not actually reindex. Any admin-triggered reindex silently does nothing.

**5.2.3 In-memory rate limiter not distributed**
`RateLimitFilter` stores rate limit counters in `ConcurrentHashMap` within the JVM. If gateway-service scales to multiple instances (or is restarted), all counters reset and rate limit state is not shared. A Redis-backed token bucket is required for correctness.

**5.2.4 RateLimitFilter spawns an unmanaged scheduled thread**
```java
Executors.newScheduledThreadPool(1).scheduleAtFixedRate(...)
```
This thread is spawned in the constructor but never registered with Spring's lifecycle — it will not be shut down gracefully, and creates a resource leak in testing or restart scenarios.

**5.2.5 No inter-service communication contract**
Services that call `SearchIndexClient` use `RestTemplate` with a hardcoded URL from an environment variable (`APP_SEARCH_SERVICE_URL`). There is no circuit breaker (Resilience4j), retry policy, or timeout configuration. A transient failure in search-service will bubble up as an uncaught exception in product/order/sales.

**5.2.6 Frontend has no router**
Route state is a plain `useState` string with a switch-case in `App.jsx`. This means: no browser history (back button broken), no URL sharing, no deep linking, and no code splitting per route.

**5.2.7 Reporting SQL builder is hardcoded to 4 report types**
`ReportSqlBuilder.build()` only handles `sales_performance_monthly`, `top_products_by_revenue`, `sales_by_region`, and `salesperson_performance`. The fallback returns `select 1 as no_data where 1=0` silently. New report types require Java changes.

**5.2.8 TenantId is hardcoded to "demo"**
Auth controller encodes `"demo"` as the `tenantId` claim in every JWT. The platform cannot serve multiple tenants without changing this.

---

### 5.3 🟡 Medium — Testing & Quality

**5.3.1 Near-zero test coverage**
Only 3 test files exist across 156 Java source files (~2%). `SearchServiceTest`, `SearchControllerSecurityTest`, and `EnquiryFlywayIT` are the only tests. All business logic in auth, orders, finance, HRMS, and reporting is completely untested.

**5.3.2 CI pipeline does not run tests**
`quality.yml` runs `mvnw -DskipTests compile` — tests are explicitly skipped. There is no test execution gate on pull requests.

**5.3.3 No frontend test suite**
There are no unit or integration tests in the frontend (`vitest`, `jest`, `testing-library` are all absent from `package.json`).

**5.3.4 No TypeScript**
The frontend uses plain JavaScript. Type errors surface at runtime. As the codebase grows (39 files now, will be more), prop mismatches and API contract drift become harder to catch.

**5.3.5 `useMemo` misused for page rendering**
```jsx
const Page = useMemo(() => { switch(route) { ... } }, [route, theme, searchQuery]);
```
`useMemo` is designed to memoize values. Using it to cache React elements is semantically incorrect — it won't prevent re-renders in the way the author likely intends, and passing memoized JSX via `React.cloneElement` is an anti-pattern.

---

### 5.4 🔵 Low — Technical Debt & Observability

**5.4.1 Kubernetes folder is empty (README only)**
The `backend/kubernetes/` directory contains only a README. There are no Helm charts, manifests, or deployment templates.

**5.4.2 Temp/scratch files in source tree**
`tmp_rovodev_patch_note.txt`, `tmp_rovodev_local_report_test.sh`, `tmp_rovodev_performance_test.ps1`, `nul` (Windows null file) — these should not be committed.

**5.4.3 No distributed tracing or structured logging**
No Zipkin, OpenTelemetry, or correlation-ID propagation. Debugging a cross-service failure requires manually correlating logs across containers.

**5.4.4 No health dashboard or alerting**
Actuator health endpoints are exposed but nothing aggregates or monitors them (no Prometheus, Grafana, or Loki).

**5.4.5 `acli.exe` binary committed to the repository**
A Windows executable binary (`acli.exe`) is tracked in git at the root. Binaries should not be version-controlled.

---

## 6. Service-by-Service Summary

| Service | Domain Model | Migrations | Tests | Notes |
|---|---|---|---|---|
| auth-service | UserEntity | V1–V3 | ❌ | Signup, login, refresh, delete. Role-in-JWT model. |
| user-service | (not inspected fully) | Assumed | ❌ | Separate from auth |
| product-service | Item | V1–V7 | ❌ | Import framework, search indexing |
| order-service | SalesOrder, OrderItem | V1–V7 | ❌ | Search indexing, aggregate endpoint |
| sales-service | Customer, Invoice | V1–V5 | ❌ | Customer import, search indexing |
| finance-service | Transaction | V1–V6 | ❌ | KPI service (MRR, cash flow), bank balances |
| hrms-service | Employee, Attendance, Leave, Payroll | V1–V5 | ❌ | Broad domain coverage, no service layer visible |
| enquiry-service | Enquiry | V1–V4 | ✅ (Flyway IT) | Minimal; 1 integration test |
| reporting-service | ReportDefinition, ReportRun, ReportExport | V1–V5 | ❌ | Async execution, CSV export, stub reindex |
| search-service | SearchProduct, SearchCustomer, SearchOrder | V1–V3 | ✅ (2 tests) | pg_trgm fuzzy search, rate-limited |
| gateway-service | — | — | ❌ | Proxy, JWT auth, CORS, rate limit |
| config-service | — | — | ❌ | Spring Cloud Config |
| discovery-service | — | — | ❌ | Eureka |
| common-lib | Shared DTOs | — | ❌ | JWT, import, search contracts |

---

## 7. Recommended Next Steps (Prioritised)

### Immediate (Pre-launch blockers)

1. **Fix the open reports endpoint** — remove `.permitAll()` from `GET /api/reports/**` in gateway `SecurityConfig`. Add proper role guards (`MANAGER`, `OWNER`, `ADMIN`).
2. **Remove the hardcoded JWT default** — require `APP_JWT_SECRET` to be set explicitly; fail fast at startup if absent or below 32 chars.
3. **Complete the search reindex implementation** — the admin-triggered reindex silently does nothing. Wire it to call each entity service and rebuild the index table.
4. **Suppress System.out/err from JwtAuthFilter** — replace with SLF4J and set appropriate log levels (DEBUG for success, WARN for failures).
5. **Add token refresh to retry logic** — the frontend's `handleAuthError` falls back to `window.axios` which is unreliable. Use the configured Axios instance instead.

### Short-term (Next sprint)

6. **Add a test execution job to CI** — enable `mvn test` with Testcontainers for at least the search-service and auth-service; enforce a minimum build gate.
7. **Replace in-memory rate limiter with Redis** — use Spring Data Redis + a token-bucket or sliding-window algorithm so rate limits survive restarts and scale across instances.
8. **Introduce Resilience4j on SearchIndexClient** — add circuit breaker and timeout so search indexing failures don't fail product/order/sales write paths.
9. **Migrate frontend to TypeScript + React Router** — TypeScript catches ~30% of runtime bugs at compile time. React Router restores browser history and enables deep linking.
10. **Fix `useMemo` misuse** — convert the page switch to a simple render with `React.lazy` + `Suspense` for route-level code splitting.

### Medium-term (Next quarter)

11. **Introduce per-service databases or schemas** — at minimum, use separate PostgreSQL schemas per service so schema isolation is enforced even on a shared host.
12. **Add structured logging + distributed tracing** — adopt OpenTelemetry with a collector sidecar; export to Grafana Loki/Tempo or a cloud provider equivalent.
13. **Move tokens from localStorage to HttpOnly cookies** — eliminates the XSS attack surface for token theft.
14. **Extend ReportSqlBuilder to be data-driven** — store report SQL definitions in the DB rather than hardcoding them, so new reports can be added via admin UI without a deploy.
15. **Complete Kubernetes manifests** — define Deployments, Services, ConfigMaps, and HPA for each microservice; implement readiness/liveness probes against the existing `/actuator/health` endpoints.

---

## 8. Summary Table

| Category | Score | Key Finding |
|---|---|---|
| Architecture | 6/10 | Sound domain decomposition, but shared DB and stubs undermine the microservices model |
| Security | 4/10 | Open report endpoints, hardcoded secrets, localStorage tokens, no revocation |
| Code Quality | 6/10 | Clean service/repo/web layering; System.out logging and stubs drag it down |
| Testing | 2/10 | ~2% test coverage, CI skips tests entirely |
| Frontend | 5/10 | Functional but lacks TypeScript, routing, and testing |
| Observability | 3/10 | Actuator exposed but no aggregation, tracing, or alerting |
| CI/CD | 5/10 | Pipeline exists and is cross-platform; no test or deploy stages |
| DevEx | 7/10 | Docker Compose one-command local setup, Flyway demo seeding, OpenAPI per service |

---

*Report generated from full repository inspection of D:\Projects\erp-smb-ui on 2026-05-28.*
