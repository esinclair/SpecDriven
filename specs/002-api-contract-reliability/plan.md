# Implementation Plan: OpenAPI Contract & Reliability Standards

**Branch**: `002-api-contract-reliability` | **Date**: 2025-12-26 | **Spec**: `specs/002-api-contract-reliability/spec.md`
**Input**: Feature specification from `/specs/002-api-contract-reliability/spec.md`

## Summary

Establish OpenAPI as the single source of truth for an unversioned public REST API, with deterministic Java generation into `src/main-gen/java`, consistent error codes, and contract-driven tests that cover both positive and mandatory negative scenarios (validation failures + server/transient errors) while communicating retryability purely via HTTP status codes and standard headers.

## Technical Context

**Language/Version**: Java 17 (Gradle toolchains; Spring Boot 3)  
**Primary Dependencies**: Spring Boot 3 (Web, Validation), OpenAPI Generator Gradle plugin 7.14.0  
**Storage**: N/A (current API is stateless `/ping`)  
**Testing**: JUnit 5 + Spring Boot Test (from `spring-boot-starter-test`)  
**Target Platform**: NEEDS CLARIFICATION (local JVM / CI; OS-agnostic)  
**Project Type**: Single Spring Boot service (Gradle)  
**Performance Goals**: ≤1s request→response for ≥95% under representative load (especially for synchronous endpoints like `/ping`)  
**Constraints**:
- OpenAPI is authoritative; generated code must not be manually edited.
- Public API is unversioned and must evolve additively only (no `/v1`, no version headers/query params/media types; no new required inputs).
- Retryability semantics are expressed only via HTTP status codes and standard headers (no `retryable` field).
- Error responses must use a stable JSON shape containing `code` and `message`.
**Scale/Scope**: Start with `/ping`, then generalize patterns (error schema + documented error responses) to future endpoints without breaking existing consumers.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Tests are mandatory: ✅ Plan includes contract + integration tests for happy path AND negative/error flows per endpoint.
- Performance budget: ✅ `/ping` stays lightweight; add a small timing-focused test/smoke check to guard the ≤1s budget.
- Error contract: ✅ Standard `ErrorResponse { code, message, details? }` already exists in OpenAPI; plan will ensure it’s used consistently with correct status codes.
- Public API compatibility: ✅ Unversioned paths (current `/ping`), additive-only evolution; no versioning mechanisms introduced.
- Build gate: ✅ Minimum green gate remains `./gradlew test` on a clean checkout (generation runs before compile/resources).

## Project Structure

### Documentation (this feature)

```text
specs/002-api-contract-reliability/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/
│   │   └── com/example/specdriven/
│   │       ├── SpecDrivenApplication.java
│   │       ├── ping/                 # handwritten controller + tests for /ping
│   │       └── error/                # error contract implementation (ControllerAdvice)
│   └── resources/
│       ├── application.properties
│       └── openapi.yaml              # SOURCE OF TRUTH
├── main-gen/
│   └── java/
│       └── com/example/specdriven/api/        # GENERATED (gitignored)
└── test/
    └── java/
        └── com/example/specdriven/
            ├── ping/
            └── error/

# reserved for generated tests (currently not generated)
src/test-gen/
└── java/
```

**Structure Decision**: Single Spring Boot project. Public API contract lives in `src/main/resources/openapi.yaml`; OpenAPI Generator emits interfaces/DTOs into `src/main-gen/java`, which is wired as a Gradle source root. `src/test-gen/java` is reserved for future generated tests.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |
