# Quickstart: Users API (Users & Roles)

Date: 2025-12-27

This quickstart is for local development and contract validation. The Users API is **feature-flagged**.

## Prerequisites

- Java 17 (per Gradle toolchain)
- Gradle wrapper (use `gradlew.bat` on Windows)

## Feature flag

Users API is gated by Spring boolean property:

- `FeatureFlag.usersApi=false` (default; feature disabled)
- `FeatureFlag.usersApi=true` (enable Users API)

When disabled, Users endpoints (including `/login`) should return **404** with the shared error body.

## Authentication (Bearer tokens)

The Users API uses Spring Security with stateless bearer tokens.

- Obtain a token via `POST /login` (this endpoint does **not** require a bearer token).
- Call protected endpoints with header: `Authorization: Bearer <token>`.

### Which endpoints require a token?

- `POST /login`: **public** (no token required).
- `POST /users`: **bootstrap-only public**
  - If there are **zero users** in the database, you may create the first user without a token.
  - After at least one user exists, creating users requires a valid bearer token.
- All other `/users*` endpoints: **always require a valid bearer token**.

Auth failures use the shared error body:
- Missing/invalid token → **401** (`code: UNAUTHORIZED`)
- Insufficient permissions (future/optional) → **403** (`code: FORBIDDEN`)

## Database configuration

Persistence is SQL-compatible and configured via Spring properties.

### Local/default (in-memory)

Use H2 in-memory for local dev. Planned properties:

- `spring.datasource.url=jdbc:h2:mem:specdriven;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`
- `spring.datasource.username=sa`
- `spring.datasource.password=`

### Non-local (external SQL)

Provide the standard Spring datasource properties for your SQL backend (e.g., PostgreSQL):

- `spring.datasource.url=jdbc:postgresql://host:5432/specdriven`
- `spring.datasource.username=...`
- `spring.datasource.password=...`

## OpenAPI-first workflow

- Update `src/main/resources/openapi.yaml` first.
- Run the OpenAPI generator (wired into build) to update generated interfaces/models.
- Implement the generated interfaces in handwritten Spring controllers/services.

## API overview

See `specs/003-users-api/contracts/openapi.contract.yaml` for the planned contract surface.

Endpoints:
- `POST /users` (bootstrap-only public; otherwise bearer-protected)
- `GET /users` (paged; requires `page` and `pageSize`; bearer-protected)
- `GET /users/{userId}` (bearer-protected)
- `PUT /users/{userId}` (bearer-protected)
- `DELETE /users/{userId}` (bearer-protected)
- `PUT /users/{userId}/roles/{roleName}` (bearer-protected)
- `DELETE /users/{userId}/roles/{roleName}` (bearer-protected)
- `POST /login` (public)

## Example requests (flow)

1) **Bootstrap** (only if zero users exist):
- `POST /users` with username/name/emailAddress/password

2) **Login**:
- `POST /login` with username/password → **200** `{ "token": "...", "tokenType": "Bearer"? }`

3) **Authenticated calls**:
- `GET /users?page=1&pageSize=20` with header `Authorization: Bearer <token>`

## Testing expectations

The implementation must include:
- happy-path integration tests for CRUD + role assignment
- happy-path integration test for login token issuance
- negative tests for 400/404/409 error codes and error body shape
- negative tests for invalid credentials → **400** with shared error body
- negative tests for missing/invalid bearer token on protected endpoints → **401** with shared error body
- tests for `FeatureFlag.usersApi` both enabled and disabled (including login)
- tests for bootstrap behavior:
  - when no users exist, unauthenticated `POST /users` succeeds
  - once a user exists, unauthenticated `POST /users` fails with **401**
