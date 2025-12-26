<!--
Sync Impact Report
- Version change: 0.0.0 → 1.0.0
- Modified principles: N/A (template placeholders replaced)
- Added sections: Additional Constraints, Development Workflow & Quality Gates
- Removed sections: N/A
- Templates requiring updates:
  - ✅ F:/data/dev/SpecDriven/.specify/templates/plan-template.md (aligned: constitution gates expectations)
  - ⚠ F:/data/dev/SpecDriven/.specify/templates/spec-template.md (no change required)
  - ⚠ F:/data/dev/SpecDriven/.specify/templates/tasks-template.md (no change required)
  - ⚠ F:/data/dev/SpecDriven/.specify/templates/checklist-template.md (no change required)
- Follow-up TODOs: None
-->

# SpecDriven Constitution

## Core Principles

### 1) Test Coverage Is Non-Negotiable
All production code MUST be covered by automated tests that include:
- Happy paths
- Exception handling
- Alternative/edge flows (validation failures, missing resources, timeouts)

Tests MUST be meaningful (assert outcomes, not only that code executes). Prefer JUnit 5 +
Spring Boot Test for integration tests and Mockito for isolated unit tests.

Rationale: This repo is spec-driven; tests are the executable form of the spec and the primary
defense against regressions.

### 2) Explicit API Contracts (Requests, Responses, Errors)
Every externally exposed endpoint/handler MUST have an explicit contract:
- Request/response DTOs are versionable and validated
- Error response shape is consistent across endpoints
- Error codes are stable identifiers (not free-form strings)

Rationale: Stable contracts allow independent testing (contract + integration tests) and reduce
breaking changes.

### 3) Performance Budget: 1 Second Service Responses
For synchronous service operations (HTTP request → response), the service MUST target ≤ 1 second
response time under expected load for the primary path.

- If an operation cannot reliably meet this budget, it MUST be redesigned (async processing,
  pagination, caching, batching) and documented in the spec.
- Performance assertions MUST be covered by automated tests where practical (e.g., a small
  performance smoke test with time budget).

Rationale: Fast responses are a product requirement and prevent slow-paths from becoming the norm.

### 4) Clear Error Codes + HTTP Status Semantics
All error responses MUST include:
- `code`: a clear, documented error code (e.g., `VALIDATION_FAILED`, `RESOURCE_NOT_FOUND`,
  `UPSTREAM_TIMEOUT`)
- `message`: human-readable summary safe to show to clients

HTTP status codes MUST communicate whether the error is retryable:
- Client errors (4xx) are generally non-retryable (except explicit cases like `429 Too Many Requests`).
- Transient server/upstream errors (e.g., timeouts, `503 Service Unavailable`) are generally retryable.
- Retry guidance SHOULD be expressed with standard headers when applicable:
  - `Retry-After` for `429` / `503` when the server can provide it

Rationale: Clients need deterministic logic for retries and support; humans need fast diagnosis.

### 5) Spring Boot & Gradle Consistency
The project MUST follow Spring Boot and Gradle conventions:
- Keep application wiring/configuration in Spring (no custom DI containers)
- Prefer Spring idioms (validation, exception handling via `@ControllerAdvice`, etc.)
- Use Gradle as the single source of build truth

Rationale: Consistency lowers cognitive load and keeps tooling straightforward.

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

## Development Workflow & Quality Gates

### Definition of Done (per change)
A change is “done” only when all are true:
- Unit tests and integration tests added/updated for happy + alternative flows
- Error-code contract is implemented and tested (including correct HTTP status semantics)
- Performance budget impact considered; any risk is documented
- Build passes on a clean checkout using the Gradle wrapper

### Required Gates
- Compile + tests MUST pass: `./gradlew test`
- If the change affects HTTP contracts, add/adjust contract tests and at least one integration
  test using Spring Boot Test.
- Exception paths MUST be explicitly tested (e.g., invalid input, missing entity, upstream failure).

### Review Expectations
Reviewers MUST verify:
- Tests cover the new behavior and failure modes
- Error codes are clear and stable
- HTTP status codes correctly reflect the error class (4xx vs 5xx, retry guidance via headers)
- Any performance-sensitive path is measured/guarded

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

**Version**: 1.0.0 | **Ratified**: 2025-12-26 | **Last Amended**: 2025-12-26
