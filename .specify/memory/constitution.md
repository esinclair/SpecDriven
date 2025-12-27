<!--
Sync Impact Report
- Version change: 1.1.0 → 1.2.0
- Modified principles:
  - None (existing principles retained)
- Added sections:
  - Core Principles: 6) Public API Compatibility: No Versioning + No Breaking Changes
- Removed sections: None
- Templates requiring updates:
  - ✅ F:/data/dev/SpecDriven/.specify/templates/plan-template.md
  - ✅ F:/data/dev/SpecDriven/.specify/templates/spec-template.md
  - ✅ F:/data/dev/SpecDriven/.specify/templates/tasks-template.md
  - ✅ F:/data/dev/SpecDriven/.specify/templates/checklist-template.md
- Follow-up TODOs:
  - TODO(RATIFICATION_DATE): Original ratification date before 2025-12-26 is unknown.
-->

# SpecDriven Constitution

## Core Principles

### 1) Test Coverage Is Non-Negotiable
All production code MUST be covered by automated tests that include:
- Happy paths
- Exception handling
- Alternative/edge flows (validation failures, missing resources, timeouts)

Negative tests are REQUIRED for:
- Invalid inputs (schema/validation failures)
- Error responses (correct status + error body shape + stable error `code`)
- Known error conditions (e.g., missing entity, upstream failure, conflict)

Tests MUST be meaningful (assert outcomes, not only that code executes). Prefer JUnit 5 +
Spring Boot Test for integration tests and Mockito for isolated unit tests.

Rationale: This repo is spec-driven; tests are the executable form of the spec and the primary
defense against regressions.

### 2) OpenAPI-First Contract + Generated Java Implementation
This project is OpenAPI-first.

- The OpenAPI document is the SOURCE OF TRUTH for the public REST API.
- Public API changes MUST be made in the OpenAPI spec first, then generated into Java, then wired
  with handwritten implementation code.
- Generated code MUST NOT be manually edited. If behavior or shapes are wrong, fix the OpenAPI spec
  and regenerate.

Rationale: The API contract must remain authoritative and reviewable. Code generation keeps the
implementation aligned with the contract.

### 3) Performance Budget: 1 Second Service Responses
For synchronous service operations (HTTP request → response), the service MUST target ≤ 1 second
response time under expected load for the primary path.

- If an operation cannot reliably meet this budget, it MUST be redesigned (async processing,
  pagination, caching, batching) and documented in the spec.
- Performance assertions SHOULD be covered by automated tests where practical (e.g., a small
  performance smoke test with time budget).

Rationale: Fast responses are a product requirement and prevent slow-paths from becoming the norm.

### 4) Error Codes + HTTP Status Indicate Retryability (No "retryable" Field)
Retryability MUST be communicated via HTTP semantics, NOT via any response field/parameter such as
`retryable`.

All error responses MUST include:
- `code`: a clear, documented error code (e.g., `VALIDATION_FAILED`, `RESOURCE_NOT_FOUND`,
  `UPSTREAM_TIMEOUT`)
- `message`: human-readable summary safe to show to clients

HTTP status codes MUST communicate whether the error is retryable:
- 4xx indicates a client problem and is NON-RETRYABLE by default.
  - Allowed explicit exceptions MUST be standard and intentional (e.g., `429 Too Many Requests`).
- 5xx indicates a server/transient problem and is RETRYABLE by default.
- Retry guidance SHOULD be expressed with standard headers when applicable:
  - `Retry-After` for `429` / `503` when the server can provide it

Rationale: Clients need deterministic retry logic without bespoke flags. HTTP already defines the
retry contract.

### 5) Spring Boot & Gradle Consistency
The project MUST follow Spring Boot and Gradle conventions:
- Keep application wiring/configuration in Spring (no custom DI containers)
- Prefer Spring idioms (validation, exception handling via `@ControllerAdvice`, etc.)
- Use Gradle as the single source of build truth

Rationale: Consistency lowers cognitive load and keeps tooling straightforward.

### 6) Public API Compatibility: No Versioning + No Breaking Changes
Public APIs MUST be stable and unversioned.

- The public HTTP surface MUST NOT use explicit versioning mechanisms:
  - No path versions like `/v1`, `/v2`.
  - No versioning via headers, query parameters, or media types.
- Backward-breaking changes to the public API are NOT ALLOWED.
  - Specifically forbidden: introducing new REQUIRED request inputs for an existing operation
    (new required JSON properties, new required query parameters, new required headers, etc.).
  - Specifically forbidden: removing or renaming request/response fields, endpoints, or changing
    semantics in a way that breaks existing clients.

Backward-compatible evolution is allowed ONLY when additive:
- Requests: adding new OPTIONAL input fields/parameters is allowed.
  - New optional fields MUST have sensible defaults and MUST NOT change behavior for clients that
    don’t send them.
- Responses: adding new output fields is allowed.
  - Added response fields MUST be optional for clients to consume.

Rationale: Clients integrate against a single stable contract. Avoiding explicit API versioning
reduces fragmentation and forces disciplined, additive evolution.

## Additional Constraints

### Technology & Structure
- Language: Java (Spring Boot)
- Build: Gradle (wrapper is required for builds)
- Use `src/main/java` and `src/test/java` conventions.

### Observability & Diagnostics
- All errors returned to clients MUST be logged server-side with enough context to debug.
- Logs MUST avoid secrets/PII.

### Backward Compatibility
- Public API changes MUST be reflected in specs and tests.
- Breaking changes MUST include a migration strategy.
- Breaking changes are NOT ALLOWED for public APIs (see Principle 6).

## Development Workflow & Quality Gates

### Definition of Done (per change)
A change is “done” only when all are true:
- OpenAPI spec updated first (when the public API changes) and code generation completed
- Unit tests and integration tests added/updated for happy + alternative flows, INCLUDING negative
  tests for invalid inputs and error responses
- Error-code contract is implemented and tested (including correct HTTP status semantics)
- Performance budget impact considered; any risk is documented
- Build passes on a clean checkout using the Gradle wrapper

### Required Gates
- Compile + tests MUST pass: `./gradlew test`
- If the change affects HTTP contracts, add/adjust contract tests and at least one integration
  test using Spring Boot Test.
- Exception paths MUST be explicitly tested (e.g., invalid input, missing entity, upstream failure).
- No response model MUST contain a `retryable` field/parameter; retry semantics MUST be conveyed
  through HTTP status codes and standard headers.
- If the change affects public HTTP contracts, verify it remains backward compatible per Principle 6
  (no new required inputs; only additive optional inputs and/or additive outputs).

### Review Expectations
Reviewers MUST verify:
- OpenAPI is the diff-of-record for API changes; generated code wasn’t hand-edited
- Tests cover the new behavior and failure modes (especially negative/error cases)
- Error codes are clear, stable, and consistent
- HTTP status codes correctly reflect the error class (4xx vs 5xx, retry guidance via headers)
- Any performance-sensitive path is measured/guarded
- Public API changes are additive-only and do not introduce explicit API versioning.

## Governance

- This constitution supersedes local conventions, task templates, and habits.
- Any PR that violates a MUST rule requires either:
  1) a fix before merge, or
  2) an explicitly approved amendment to this constitution.

### Amendment Process
- Propose amendments as a PR that updates this file.
- The PR description MUST include:
  - Motivation
  - Scope of change
  - Backward compatibility impact
  - Version bump rationale (see below)

### Versioning Policy (Constitution)
- MAJOR: incompatible principle changes (removals or substantive redefinition)
- MINOR: new principles/sections or materially expanded guidance
- PATCH: clarifications/wording/typos with no semantic governance change

### Compliance Review
- At least quarterly (or when repeated exceptions occur), review whether rules are still correct
  and feasible.

**Version**: 1.2.0 | **Ratified**: TODO(RATIFICATION_DATE): original adoption date unknown | **Last Amended**: 2025-12-26
