# Data Model: Users API (Users & Roles)

Date: 2025-12-27  
Source: `specs/003-users-api/spec.md`

This is the Phase 1 design output describing entities, fields, constraints, and relationships.

## Authentication / Authorization (behavioral model)

### Bearer token

- Token is minted by `POST /login` (no bearer token required).
- Token is presented as an HTTP header: `Authorization: Bearer <token>`.
- Token validation is performed by Spring Security on protected endpoints.

### Protected endpoints (bootstrap exception)

- **Always public**: `POST /login`.
- **Conditionally public**: `POST /users` is allowed without a bearer token **only when there are zero users** in the database (**bootstrap**).
- **Always protected**: all other `/users*` endpoints require a valid bearer token.

### Auth-related errors

All auth failures use the shared error response shape (`code`, `message`):

- **401 UNAUTHORIZED**: missing / invalid / expired bearer token → `code: UNAUTHORIZED`
- **403 FORBIDDEN**: valid token but caller lacks permission (if/when permission checks are implemented) → `code: FORBIDDEN`

## Entity: User

### Fields

| Field | Type | Required | Notes / Validation |
|------|------|----------|---------------------|
| `id` | UUID (string) | server-assigned | Immutable. Returned for all reads. |
| `username` | string | yes | Non-blank; suggested max length 50; unique (recommended). |
| `name` | string | yes | Non-blank; suggested max length 200. |
| `emailAddress` | string | yes | Must be a valid email; suggested max length 254; **unique**. |
| `password` | string | request-only | Required on create; optional on update. Stored as secure hash; never returned. |
| `roles` | Role[] | in responses | Returned on reads/list. Order not significant. |

### Notes
- The API uses separate request DTOs:
  - `CreateUserRequest` includes `password`
  - `UpdateUserRequest` may include `password`
  - `User` response never includes `password`

### State transitions
- Create: `CreateUserRequest` → `User`
- Update: patch-like update (only provided fields are changed), but implemented via `PUT` with optional fields in request to remain backward compatible.
- Delete: hard delete (per spec) resulting in 404 for subsequent reads.

## Entity: Role

### Fields

| Field | Type | Required | Notes |
|------|------|----------|------|
| `roleName` | string (enum) | yes | Constrained set (`RoleName`). Unknown values rejected with 400. |
| `permissions` | Permission[] | yes | Constrained set (`Permission`). Must be unique within role. |

## Entity: Permission

### Fields

| Field | Type | Required | Notes |
|------|------|----------|------|
| value | string (enum) | n/a | Enumerated capabilities. |

## Relationship: User ↔ Role

- Many-to-many: a user can have multiple roles; a role can be assigned to multiple users.
- Storage: `users`, `roles`, and join table `user_roles`.

## Persistence schema (SQL)

### Table: `users`

- `id` UUID (PK)
- `username` VARCHAR NOT NULL
- `name` VARCHAR NOT NULL
- `email_address` VARCHAR NOT NULL UNIQUE
- `password_hash` VARCHAR NOT NULL
- timestamps optional (out of scope unless repo has a convention)

Suggested indexes:
- UNIQUE(`email_address`)
- UNIQUE(`username`) (recommended)

### Table: `roles`

- `role_name` VARCHAR (PK)  
  (or a surrogate `id` + unique constraint; but PK on name keeps catalog simple)

### Table: `role_permissions`

- `role_name` VARCHAR (FK → roles.role_name)
- `permission` VARCHAR
- UNIQUE(`role_name`, `permission`)

### Table: `user_roles`

- `user_id` UUID (FK → users.id)
- `role_name` VARCHAR (FK → roles.role_name)
- UNIQUE(`user_id`, `role_name`) to enforce idempotency

## Catalog constraints

- Roles are a known, finite set defined by contract (OpenAPI enum).
- Permissions are a known, finite set defined by contract (OpenAPI enum).
- On boot, ensure the role catalog exists (either pre-seeded in migration SQL, or validated in code).
