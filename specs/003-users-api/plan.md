# Implementation Plan: Users API (Users & Roles) — Authentication/Authorization

**Branch**: `003-users-api` | **Date**: 2025-12-27 | **Spec**: `specs/003-users-api/spec.md`
**Input**: Feature specification from `specs/003-users-api/spec.md`

## Summary

Implement the Users API as an OpenAPI-first Spring Boot feature, now including authentication/authorization requirements:

- Use Spring Security to mint and validate **Bearer tokens**.
- `POST /login` does **not** require a bearer token.
- **Bootstrap rule**: if there are **zero users** in the database, `POST /users` may be called without a bearer token to create the first user.
- Otherwise, all other Users endpoints require a valid bearer token (explicit decision: once at least one user exists, `POST /users` also requires a token).

All endpoints remain feature-flagged behind `FeatureFlag.usersApi` and use the shared error response (`code`, `message`) with correct HTTP semantics.

## Technical Context

**Language/Version**: Java 17 (Gradle toolchain)  
**Primary Dependencies**: Spring Boot 3.5.x, Spring Web, Spring Validation, Spring Security, OpenAPI Generator  
**Storage**: SQL-compatible DB via Spring datasource properties; local-dev default H2 in-memory  
**Testing**: JUnit 5, Spring Boot Test  
**Target Platform**: Server (Spring Boot)  
**Project Type**: Single Spring Boot service (generated API interfaces + handwritten implementation)  
**Performance Goals**: ≤ 1s request→response for primary paths (CRUD/list/login)  
**Constraints**: OpenAPI-first; stable error codes; retry semantics via HTTP only; no API versioning; additive changes only; all endpoints feature-flagged  
**Scale/Scope**: Small admin-style API with paging and role assignment

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Tests are mandatory**: planned happy-path + negative tests include validation errors, missing resources, conflict, feature-flag disabled behavior, invalid credentials, and missing/invalid bearer token.
- **Performance budget**: CRUD/list/login are synchronous and expected to be sub-1s on local H2; keep queries indexed + paged.
- **Paged results**: `GET /users` is paged with required `page` and `pageSize` and includes page metadata.
- **Error contract**: all 4xx/5xx return the shared `ErrorResponse` and use HTTP status for retry semantics; `503` may include `Retry-After`.
- **Public API compatibility**: auth is introduced via OpenAPI `securitySchemes` and `security` requirements (additive); no versioning.
- **Feature flagging**: `FeatureFlag.usersApi` gates all Users endpoints, including `/login`.
- **Build gate**: `./gradlew test` must pass.

## Project Structure

### Documentation (this feature)

```text
specs/003-users-api/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.contract.yaml
└── tasks.md   # created by /speckit.tasks (not part of this update)
```

### Source Code (repository root)

```text
src/
  main/
    java/com/example/specdriven/
    resources/
      openapi.yaml
  main-gen/
    java/   # openapi-generator output

test/
  java/com/example/specdriven/
```

**Structure Decision**: Single Spring Boot service. Public API is generated from `src/main/resources/openapi.yaml` into `src/main-gen/java`, with handwritten wiring in `src/main/java`.

## Phase 0: Outline & Research (completed)

Artifacts:
- `specs/003-users-api/research.md`

Key decisions added/confirmed for this update:
- Bearer tokens via Spring Security
- `/login` public
- bootstrap exception for unauthenticated `POST /users` only when there are zero users
- once at least one user exists, `POST /users` requires token (explicit assumption encoded)

## Phase 1: Design & Contracts (completed)

Artifacts:
- `specs/003-users-api/data-model.md`
- `specs/003-users-api/contracts/openapi.contract.yaml`
- `specs/003-users-api/quickstart.md`

### Contract changes needed for Auth

1) OpenAPI `components.securitySchemes.bearerAuth` added.
2) Global security requirement added (bearerAuth) for Users endpoints.
3) `/login` explicitly sets `security: []`.
4) `POST /users` documents bootstrap exception and uses a per-operation `security` block allowing either:
   - a bearer token, or
   - no security (bootstrap-only)
5) Protected endpoints document `401`/`403` responses using shared `ErrorResponse`.

### Edge cases to explicitly handle (and test)

- Missing `Authorization` header on protected endpoints → 401 UNAUTHORIZED with shared error body.
- Malformed `Authorization` header (not `Bearer <token>`) → 401.
- Invalid/expired token → 401.
- Feature flag disabled for any endpoint (including `/login`) → 404 with shared error body.
- Bootstrap create user:
  - when user count == 0, unauthenticated create succeeds (201)
  - when user count > 0, unauthenticated create fails (401)

## Phase 1: Agent Context Update

Run:
- `.specify/scripts/powershell/update-agent-context.ps1 -AgentType copilot`

Expected outcome:
- Agent context file updated with newly introduced technology in this plan (Spring Security + bearer auth), without overwriting manual notes.

## Phase 2: Planning (stop point for speckit.plan)

Implementation work items to be tracked in `tasks.md` (not generated here):

- Update `src/main/resources/openapi.yaml` to match `contracts/openapi.contract.yaml` (security schemes + 401/403 + bootstrap rule docs).
- Regenerate code (Gradle `openApiGenerate`).
- Add Spring Security configuration:
  - token minting in `/login`
  - token validation filter for `/users*`
  - allow unauthenticated `POST /users` only when user count == 0
- Add tests:
  - login success and invalid-credentials (400) non-enumerating
  - protected endpoint without token (401)
  - bootstrap create user behavior (unauthenticated allowed only for first user)
  - feature flag disabled mode returns 404


