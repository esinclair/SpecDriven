# Phase 0 Research: Users API (Users & Roles) — AuthN/AuthZ update

Date: 2025-12-27  
Feature: `specs/003-users-api/spec.md`

This document resolves repo-specific unknowns and records key technical decisions using the required format.

## Decision 1: Java / Spring Boot baseline

- **Decision**: Use Java **17** (Gradle toolchain) and Spring Boot **3.5.x**.
- **Rationale**: `build.gradle` pins the toolchain to Java 17 and uses Spring Boot 3.5.9. Aligning feature work to these avoids drift.
- **Alternatives considered**:
  - Java 21 (rejected: repo toolchain is 17 today; switching is out of scope for this feature).

## Decision 2: OpenAPI-first workflow and where to define the contract

- **Decision**: Define Users API endpoints and schemas in `src/main/resources/openapi.yaml` and generate interfaces/models into `src/main-gen/java` via the existing `openApiGenerate` Gradle task.
- **Rationale**: Constitution Principle 2 requires OpenAPI-first. `build.gradle` already wires `openApiGenerate` to run before `compileJava` and adds `src/main-gen/java` to the `main` sourceSet.
- **Alternatives considered**:
  - Handwritten controllers + DTOs (rejected: violates OpenAPI-first + generated-code policy).

## Decision 3: Error contract and error codes

- **Decision**: Reuse the existing `ErrorResponse` schema in OpenAPI and the existing server-side error code enum pattern (`ApiErrorCode`). Extend it with Users-API-specific stable codes as needed (notably a conflict code for uniqueness violations; auth failures get dedicated codes).
- **Rationale**: The repo already has `ErrorResponse` (required fields: `code`, `message`) and a `GlobalExceptionHandler` that produces `VALIDATION_FAILED` and `INTERNAL_ERROR`, matching Constitution Principle 4.
- **Alternatives considered**:
  - New error response schema for Users API (rejected: violates shared error-shape requirement FR-030).
  - Adding a `retryable` boolean field (rejected: explicitly forbidden).

## Decision 4: Feature-flag gating mechanism

- **Decision**: Gate Users API behavior behind Spring boolean property `FeatureFlag.usersApi` (default `false`). When disabled, endpoints return **404** with the shared error body.
- **Rationale**: Constitution Principle 8 requires Spring-property feature flags with exact naming. The feature spec explicitly calls out `FeatureFlag.usersApi` with default disabled.
- **Alternatives considered**:
  - Remove endpoints when disabled (rejected: OpenAPI contract must remain the source of truth and codegen will still publish interfaces; runtime routing changes are harder to test).
  - 501 Not Implemented (rejected: implies permanent lack of support; not a rollout flag).

## Decision 5: Pagination pattern for list endpoints

- **Decision**: Use required query parameters `page` (1-based) and `pageSize` (bounded 1..100) and return a paged response schema that includes:
  - `items: User[]`
  - `page`, `pageSize`
  - `totalItems`
  - `totalPages`
- **Rationale**: Constitution Principle 7 + spec FR-015 require pagination inputs and a paged response whenever multiple items may be returned.
- **Alternatives considered**:
  - Offset/limit (rejected: fine, but this repo has no established pattern; page/pageSize is simpler for contract readability).
  - Cursor pagination (rejected: more complex than needed for this feature; can be added later as additive optional inputs).

## Decision 6: SQL persistence + local in-memory DB

- **Decision**: Use a SQL-compatible persistence layer configured via Spring properties, with H2 in-memory by default for local dev.
- **Rationale**: Feature requirements mandate SQL-compatible backend and Spring properties for DB connection.
- **Alternatives considered**:
  - In-memory maps only (rejected: violates persistence requirement).
  - NoSQL (rejected: violates requirement).

## Decision 7: Persistence stack (JPA vs JDBC)

- **Decision**: Use **Spring Data JDBC** unless the repo already standardizes on JPA.
- **Rationale**: There are currently no DB dependencies in `build.gradle`. Spring Data JDBC is simpler than JPA/Hibernate for CRUD and avoids ORM complexity.
- **Alternatives considered**:
  - Spring Data JPA/Hibernate (rejected for now: adds ORM complexity; fine later if needed).

## Decision 8: Filtering for list users

- **Decision**: Support explicit filters as query parameters:
  - `username` (exact match)
  - `emailAddress` (exact match)
  - `name` (case-insensitive contains match)
  - `roleName` (exact match)
- **Rationale**: Meets FR-017/FR-018 while keeping implementation straightforward.
- **Alternatives considered**:
  - Arbitrary filter expressions (rejected: difficult to validate; violates unknown query param → 400 rule).

## Decision 9: Password handling

- **Decision**: Treat `password` as an input field for create/update but **do not** return it in user responses (store a hash only). In OpenAPI, model request bodies separately (`CreateUserRequest`, `UpdateUserRequest`) from `User` response.
- **Rationale**: Returning passwords is unsafe. The spec requires the field exists, but it doesn’t require echoing it back.
- **Alternatives considered**:
  - Return password in `User` response (rejected: leaks secrets).

## Decision 10: Uniqueness constraints

- **Decision**: Enforce uniqueness on `emailAddress` (and optionally `username`) at the DB layer (unique index) and surface conflicts as **409** with a stable code (suggested `RESOURCE_CONFLICT`).
- **Rationale**: Spec FR-033 requires 409 on uniqueness violations and assumes unique email.
- **Alternatives considered**:
  - Application-only uniqueness checks (rejected: race conditions).

## Decision 11: Authentication mechanism (Bearer tokens via Spring Security)

- **Decision**: Use **Spring Security** with a stateless **Bearer token** scheme, minted by our `/login` endpoint and validated on protected Users endpoints.
- **Rationale**: Requirement explicitly calls for Spring Security and Bearer tokens. A stateless token (e.g., JWT) avoids server-side session storage and keeps horizontal scaling simple.
- **Alternatives considered**:
  - HTTP Basic auth (rejected: credentials sent on every request; weaker client UX).
  - Cookie sessions (rejected: stateful, CSRF concerns, not aligned with “Bearer token” requirement).

## Decision 12: Which endpoints require authentication (bootstrap rule)

- **Decision**:
  - `/login` **never** requires a bearer token.
  - `POST /users` is **conditionally unauthenticated** only when there are **zero users** in the database (“bootstrap admin”), otherwise it **requires a valid bearer token**.
  - All other `/users*` endpoints **always** require a valid bearer token.
- **Rationale**: Meets the stated bootstrap requirement while preventing unauthenticated user creation once the system is initialized.
- **Alternatives considered**:
  - Always allow unauthenticated create user (rejected: turns user creation into a public signup endpoint; not requested).
  - Always require auth for create user (rejected: system becomes impossible to bootstrap without out-of-band DB seeding).

## Decision 13: Auth failure statuses and error codes

- **Decision**: For protected endpoints, missing/invalid/expired bearer token returns **401** with shared `ErrorResponse` and stable code `UNAUTHORIZED`. For valid token without required permission (if/when enforced), return **403** with shared `ErrorResponse` and code `FORBIDDEN`.
- **Rationale**: Standard HTTP semantics; keeps error shape consistent with FR-030/Constitution Principle 4.
- **Alternatives considered**:
  - Return 400 for missing/invalid token (rejected: conflates auth with validation; breaks standard client handling).
  - Return 404 to hide endpoints (rejected: spec only requires 404 for feature-flag gating; auth should be explicit).

## Decision 14: OpenAPI representation of security

- **Decision**: Add an OpenAPI `securitySchemes` entry for `bearerAuth` (`type: http`, `scheme: bearer`) and declare security requirements:
  - Global security requirement for Users endpoints.
  - Override to `security: []` for `/login`.
  - Document bootstrap exception for `POST /users` in the operation description.
- **Rationale**: Keeps the contract OpenAPI-first and makes auth requirements visible to clients.
- **Alternatives considered**:
  - Leaving security undocumented in OpenAPI (rejected: violates “OpenAPI is source of truth”).


