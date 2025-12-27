# Feature Specification: Users API (Users & Roles)

**Feature Branch**: `003-users-api`  
**Created**: 2025-12-27  
**Status**: Draft  
**Input**: User description: "Users API that manages User objects and their Roles. A user has username, name, password, email address. A Role has a role name and a set of Permissions."

## Clarifications

### Session 2025-12-27

- Q: What is the login token response shape, what status code is used for invalid credentials, how does feature-flag gating behave when disabled, and what security-sensitive behavior is required (e.g., not leaking whether username exists)? → A: Login returns JSON `{ "token": "<string>" }` (optionally also `tokenType: "Bearer"`); invalid credentials return **400** with a generic, non-enumerating error (same response for unknown username vs wrong password); when feature flag is disabled, endpoints return **404** with standard error; error messages/codes must not reveal whether the username exists.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create and view a user (Priority: P1)

As an API consumer, I want to create a new user and retrieve that user by ID so I can onboard users and confirm their current details.

**Why this priority**: Creating and reading users is the foundation for any user management capability.

**Independent Test**: Can be fully tested by creating a user, retrieving it by ID, and verifying the response matches the contract and includes roles.

**Acceptance Scenarios**:

1. **Given** a valid user payload with required fields, **When** I create the user, **Then** the response is **201** with the created user representation including a server-assigned `id`.
2. **Given** an existing user ID, **When** I retrieve the user by ID, **Then** the response is **200** with the user representation.

**Negative/Error Scenarios**:

1. **Given** a create request missing required fields or with invalid field formats, **When** I create the user, **Then** the response is **400** with the standard error body containing `code` and `message`.
2. **Given** a non-existent user ID, **When** I retrieve the user by ID, **Then** the response is **404** with the standard error body containing `code` and `message`.
3. **Given** a transient failure prevents processing the request, **When** I create or retrieve a user, **Then** the response is **503** with the standard error body and MAY include `Retry-After`.

---

### User Story 2 - Update and delete a user (Priority: P1)

As an API consumer, I want to update user attributes and delete users so I can keep the directory accurate and remove users when appropriate.

**Why this priority**: Lifecycle management requires both updates and removals.

**Independent Test**: Can be tested by creating a user, updating it, verifying changes through retrieval, then deleting it and verifying it no longer exists.

**Acceptance Scenarios**:

1. **Given** an existing user and a valid update payload, **When** I update the user, **Then** the response is **200** with the updated user representation.
2. **Given** an existing user, **When** I delete the user, **Then** the response is **204** and retrieving the user afterwards returns **404**.

**Negative/Error Scenarios**:

1. **Given** an update payload with invalid values, **When** I update the user, **Then** the response is **400** with standard error `code` and `message`.
2. **Given** a non-existent user ID, **When** I update or delete that user, **Then** the response is **404** with standard error `code` and `message`.
3. **Given** a client attempts to change an immutable/system-controlled field (for example `id`), **When** I update the user, **Then** the response is **400** with standard error `code` and `message`.

---

### User Story 3 - List and filter users (Priority: P2)

As an API consumer, I want to retrieve users in bulk (all users or a filtered subset) using required pagination so I can browse and locate users efficiently without receiving unbounded response sizes.

**Why this priority**: Listing and filtering are necessary for admin UIs and operational tooling, but can ship after core CRUD.

**Independent Test**: Can be tested by creating multiple users, listing them with and without filters, and verifying paging correctness and metadata across multiple pages.

**Acceptance Scenarios**:

1. **Given** multiple users exist and I provide valid pagination parameters, **When** I list users without any filter criteria, **Then** the response is **200** and contains a page of users plus paging metadata.
2. **Given** multiple users exist and I provide valid pagination parameters, **When** I list users with one or more supported filter criteria, **Then** the response is **200** and contains only users that match all provided filter criteria.
3. **Given** the number of matched users exceeds the requested page size, **When** I request the first page, **Then** the response contains at most `pageSize` users and paging metadata indicates how to retrieve at least one subsequent page.
4. **Given** the list operation can return more than one user, **When** I call the list endpoint, **Then** pagination parameters are required and the response is paged (no unbounded full-list responses).

**Negative/Error Scenarios**:

1. **Given** invalid pagination parameters (negative, zero where not allowed, or too large), **When** I list users, **Then** the response is **400** with standard error `code` and `message`.
2. **Given** I omit required pagination parameters, **When** I list users, **Then** the response is **400** with standard error `code` and `message`.
3. **Given** a request uses unsupported/unknown query parameters, **When** I list users, **Then** the response is **400** with standard error `code` and `message` (default behavior).

---

### User Story 4 - Assign and remove roles for a user (Priority: P2)

As an API consumer, I want to assign roles to a user and remove roles from a user so I can control access and permissions based on roles.

**Why this priority**: Role assignment is critical for authorization use cases, but it logically follows after user CRUD is available.

**Independent Test**: Can be tested by creating a user, assigning roles, verifying they appear on the user resource, then removing a role and verifying the update.

**Acceptance Scenarios**:

1. **Given** an existing user and a known role, **When** I assign the role to the user, **Then** the response is **200** (or **204**) and retrieving the user shows the role assigned.
2. **Given** an existing user with an assigned role, **When** I remove the role from the user, **Then** the response is **200** (or **204**) and retrieving the user no longer shows that role.

**Negative/Error Scenarios**:

1. **Given** a non-existent user ID, **When** I assign or remove a role, **Then** the response is **404** with standard error `code` and `message`.
2. **Given** a request references an unknown role, **When** I assign that role, **Then** the response is **400** with standard error `code` and `message`.
3. **Given** the same role is assigned again, **When** I assign it, **Then** the operation is idempotent (no duplicate roles) and returns **200** (or **204**) without error.

---

### User Story 5 - Login and obtain an authentication token (Priority: P1)

As an API consumer, I want to authenticate a user with `username` and `password` so I can obtain a Spring Security token for subsequent authenticated requests.

**Why this priority**: Many consumers need a first-class way to exchange credentials for an authentication token before they can call protected endpoints.

**Independent Test**: Can be tested by creating a user with known credentials, calling the login endpoint with those credentials, and verifying a token is returned; then verifying invalid credential variants return **400** with the shared error shape.

**Acceptance Scenarios**:

1. **Given** an existing user and a correct `username` + `password`, **When** I call the login endpoint, **Then** the response is **200** with a non-empty Spring Security token (as defined by the OpenAPI contract).
2. **Given** an existing user and a correct `username` + `password`, **When** I call the login endpoint multiple times, **Then** each response is **200** and each response contains a non-empty token.

**Negative/Error Scenarios**:

1. **Given** a login request body missing `username` or `password`, **When** I call the login endpoint, **Then** the response is **400** with standard error `code` and `message`.
2. **Given** a login request with an invalid `username` format or invalid `password` format (for example empty/whitespace-only), **When** I call the login endpoint, **Then** the response is **400** with standard error `code` and `message`.
3. **Given** a login request where no user matches the provided `username`, **When** I call the login endpoint, **Then** the response is **400** with standard error `code` and `message`, and the response MUST NOT indicate whether the username exists.
4. **Given** a login request where the `username` matches an existing user but the `password` is incorrect, **When** I call the login endpoint, **Then** the response is **400** with standard error `code` and `message`, and the response MUST NOT indicate whether the username exists.
5. **Given** a transient failure prevents processing login, **When** I call the login endpoint, **Then** the response is **503** with the standard error body and MAY include `Retry-After`.

---

### Edge Cases

- Duplicate create attempts for a unique identifier (e.g., same email) and how conflicts are represented
- Updates that remove all roles vs leaving roles unchanged
- Very long strings (names/emails) and empty/whitespace-only values
- Pagination boundaries (empty last page, limits at maximum allowed value)
- Unknown/extra fields in JSON payloads (default behavior: **400** invalid input)
- Login with leading/trailing whitespace in `username` (expectation: treated according to contract-defined validation rules)
- Login attempts with very long `username`/`password` values (expectation: rejected as invalid input if outside documented constraints)
- Multiple failed login attempts for the same username (expectation: returns stable **400** error response that does not indicate whether the username exists; rate limiting / lockout policy is out of scope unless later specified)
- Retry semantics differences:
  - **4xx** indicates the client should not retry without changing the request
  - **503** indicates retry may succeed; server MAY include `Retry-After`
- Listing without filters (all users) when the total is large (must still be paged)
- Filtering that matches zero users (valid **200** response with empty results page)

## Requirements *(mandatory)*

### Functional Requirements

#### Contract & Compatibility

- **FR-001 (OpenAPI source of truth)**: The Users API MUST be specified in the OpenAPI document located at `src/main/resources/openapi.yaml`.
- **FR-002 (Unversioned paths)**: All Users API paths MUST be unversioned (no `/v1` style segments) and MUST NOT introduce versioning via headers, query parameters, or media types.
- **FR-003 (Backward compatibility)**: All future changes to these endpoints and schemas MUST be additive-only and backward compatible (no new required inputs on existing operations; no removals/renames of existing fields).

#### User Resource (CRUD)

- **FR-010 (Create user)**: The system MUST allow creating a user and return **201** with the created user including a server-assigned unique `id`.
- **FR-011 (Get user)**: The system MUST allow retrieving a user by `id` and return **200** with the user representation when found.
- **FR-012 (Update user)**: The system MUST allow updating a user by `id` and return **200** with the updated user representation.
- **FR-013 (Delete user)**: The system MUST allow deleting a user by `id` and return **204** on success.
- **FR-014 (List users)**: The system MUST provide an endpoint to list users that can return all users or a subset of users based on filter criteria.
- **FR-015 (Pagination required for multi-user results)**: The list users operation MUST be paged whenever it may return more than one user. The OpenAPI contract MUST require pagination input parameters for the list operation.
- **FR-016 (Page size enforcement)**: When listing users, the API MUST return no more than the number of users requested by the page size parameter.
- **FR-017 (Filtering)**: The list users operation MUST support returning a subset of users based on filter criteria provided via documented query parameters. If multiple filter criteria are provided, the operation MUST apply all provided criteria (logical AND).
- **FR-018 (Filter parameter contract)**: The OpenAPI contract MUST explicitly document which filter parameters are supported, their formats, and whether matching is exact or partial for each parameter.
- **FR-019 (User fields)**: The User object MUST include the following fields:
  - `username`
  - `name`
  - `password`
  - `emailAddress`

#### Roles

- **FR-020 (Roles on user)**: The user representation MUST include the user’s assigned roles.
- **FR-021 (Assign role)**: The system MUST allow assigning a role to a user and MUST NOT create duplicate role assignments (idempotent behavior for repeated assignment of the same role).
- **FR-022 (Remove role)**: The system MUST allow removing a role from a user; removing a role that is not assigned MUST be treated as idempotent (no-op) and MUST NOT create an error.
- **FR-023 (Role catalog constraints)**: The API MUST validate role values against a known set of roles defined by the contract (for example an enumerated list) or by a documented policy.
- **FR-024 (Role fields)**: A Role MUST include:
  - `roleName`
  - `permissions` (a set/list of Permission values)

#### Authentication (Login)

- **FR-025 (Login endpoint)**: The Users API MUST provide a login endpoint that accepts `username` and `password` in the request body.
- **FR-026 (Successful login token)**: When the provided credentials match an existing user, the login endpoint MUST return **200** with a valid Spring Security token, as represented by the OpenAPI contract.
- **FR-026a (Token response shape)**: The OpenAPI contract MUST represent the login success response as JSON containing at minimum a non-empty string field `token`. If a token type is included, use `tokenType: "Bearer"`.
- **FR-027 (Invalid credentials)**: When no user matches the provided `username` OR the password is invalid, the login endpoint MUST return **400** with the shared error response containing `code` and `message`.
- **FR-027a (No credential enumeration)**: For invalid-credential responses, the API MUST return an indistinguishable response (same HTTP status code and same error `code`) for unknown username vs incorrect password, and MUST NOT leak whether the username exists.
- **FR-028 (Login input validation)**: For missing required fields, invalid formats, unknown/extra fields, or values outside documented constraints in the login request body, the login endpoint MUST return **400** with a stable validation-related error `code`.

#### Validation & Errors (shared error shape)

- **FR-030 (Shared error format)**: All **4xx** and **5xx** responses for Users API endpoints MUST use the existing standard error response shape containing at minimum `code` and `message`.
- **FR-031 (Validation failures)**: For invalid request bodies, invalid path/query parameters, missing required fields, unknown/extra fields, and attempts to modify immutable fields, the API MUST return **400** with a stable validation-related error `code`.
- **FR-032 (Not found)**: For operations targeting a user that does not exist, the API MUST return **404** with a stable not-found error `code`.
- **FR-033 (Conflict)**: When creating or updating a user would violate a uniqueness constraint (assumption: unique `email`), the API MUST return **409** with a stable conflict error `code`.

#### Retry Semantics (HTTP status codes only)

- **FR-040 (No retryable field)**: The Users API MUST NOT add any request/response field or parameter for retry guidance (for example `retryable`).
- **FR-041 (Retry guidance via HTTP only)**: Retry guidance MUST be communicated only through HTTP status codes and standard headers.
- **FR-042 (Transient failures)**: For transient failures where a retry may succeed, the API MUST return **503** and MAY include `Retry-After`.

#### Feature Flagging

- **FR-050 (Feature flag gating)**: All Users API endpoints described in this specification, including login, MUST be gated behind feature flagging.
- **FR-050a (Disabled behavior)**: When the feature flag gating a Users API endpoint is disabled, that endpoint MUST respond with **404** and the shared error response body; responses MUST NOT reveal that the endpoint exists or is merely disabled.
- **FR-051 (Flag name)**: The default feature flag is `FeatureFlag.usersApi`.
  - If the implementation needs to decouple login rollout from the rest of the Users API, a dedicated login flag MAY be introduced (for example `FeatureFlag.usersLogin`) but MUST be documented in this specification and in the OpenAPI description fields.

### Acceptance Criteria (by Requirement)

- **AC-001 (for FR-001)**: `src/main/resources/openapi.yaml` contains the Users API endpoints, schemas, and responses as described in this specification.
- **AC-002 (for FR-010)**: Creating a valid user returns **201** and includes `id` and roles.
- **AC-003 (for FR-011)**: Retrieving an existing user returns **200**; retrieving a non-existent user returns **404** with error `code` and `message`.
- **AC-004 (for FR-012)**: Updating a user returns **200** and reflects changes; updating with invalid values returns **400**.
- **AC-005 (for FR-013)**: Deleting a user returns **204**; subsequent retrieval returns **404**.
- **AC-006 (for FR-014/FR-015/FR-016)**: The OpenAPI contract defines a list users operation where pagination parameters are required. Listing users returns **200** with a paged result; omitting required pagination parameters returns **400**; and the number of returned users is never greater than the requested page size.
- **AC-006a (for FR-017/FR-018)**: When supported filter criteria are provided, the list response contains only users matching all provided criteria. The OpenAPI contract documents supported filters and their matching rules; unsupported query parameters return **400**.
- **AC-007 (for FR-021/FR-022)**: Assigning an already-assigned role and removing a non-assigned role are both idempotent and do not result in duplicates or errors.
- **AC-008 (for FR-030)**: All documented error responses use the shared error schema with `code` and `message`.
- **AC-009 (for FR-033)**: Creating/updating with an `email` that conflicts returns **409** with a stable conflict error code.
- **AC-010 (for FR-042)**: Simulated transient failures return **503** (optionally with `Retry-After`).
- **AC-011 (for FR-019)**: The OpenAPI contract defines the User schema with `username`, `name`, `password`, and `emailAddress`, and validation rules cause invalid/missing values to return **400**.
- **AC-012 (for FR-025/FR-026/FR-026a)**: The OpenAPI contract defines a login operation that accepts `username` and `password`. With correct credentials for an existing user, it returns **200** with a non-empty token field `token`.
- **AC-013 (for FR-027/FR-028)**: With an unknown username, invalid password, missing required fields, invalid formats, or unknown/extra fields, the login endpoint returns **400** with the shared error shape (`code`, `message`).
- **AC-014 (for FR-050/FR-050a/FR-051)**: When `FeatureFlag.usersApi` is disabled, Users API endpoints (including login) return **404** with the shared error body; when enabled, they are available. If a dedicated login flag is added, the spec and OpenAPI description clearly document which flag gates login.

### Key Entities *(include if feature involves data)*

- **User**: A person/principal represented in the system. Required fields: `username`, `name`, `password`, `emailAddress`. The representation also includes a system-assigned `id` and assigned `roles`.
- **Role**: A named permission group assigned to users. Fields: `roleName` and `permissions` (a set/list of Permission values).
- **Permission**: A named capability value included in a Role’s `permissions` set. This is represented as a constrained set (assumption: enumerated in the contract).
- **Login Request**: A request payload containing `username` and `password`.
- **Login Response**: A response payload containing a Spring Security token. The canonical response shape is a JSON object with `token: string` (and optionally `tokenType: "Bearer"`).
- **Error Response**: The shared error payload returned for **4xx/5xx** responses, containing stable `code` and `message`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001 (CRUD capability)**: A consumer can create, retrieve, update, list, and delete users end-to-end using only the published API contract, with 100% of operations returning the documented status codes and response shapes.
- **SC-002 (Role management)**: A consumer can assign and remove roles for a user, with role assignment/removal idempotency validated across repeated requests.
- **SC-003 (Error consistency)**: For a defined suite of invalid requests (missing required fields, invalid formats, unknown fields, invalid pagination, unknown role, invalid login), 100% of responses include the shared error shape with `code` and `message`.
- **SC-004 (Retry semantics correctness)**: For a defined suite of transient-failure scenarios, 100% of responses use **503** (and optionally `Retry-After`), and no client-visible retry guidance fields exist in request/response payloads.
- **SC-005 (Compatibility readiness)**: The contract has no versioning in paths/headers, and a review confirms changes to Users API can be made additively without breaking existing consumers.
- **SC-006 (Paged listing behavior)**: For a dataset of at least 100 users, a consumer can retrieve all users by iterating pages, and each page returns no more than the requested page size while preserving the documented filter behavior.
- **SC-007 (Token issuance flow)**: For a test user with known credentials, a consumer can obtain a non-empty Spring Security token via the login endpoint in under 30 seconds end-to-end (create user if needed, then login), and invalid-credential attempts consistently return **400** with the shared error shape.

## Assumptions

- The API is a JSON-over-HTTP API, and OpenAPI is the only source of truth for request/response schemas.
- Users are uniquely identifiable by a system-assigned `id`.
- Email is treated as a unique attribute used for conflict detection.
- Roles are a constrained set; unknown roles are rejected with **400**.
- Feature flagging: the Users API feature is gated by application configuration `FeatureFlag.usersApi`.
  - `true` enables the feature, `false` disables it.
  - Default value: `false` (until implementation is validated end-to-end).
- Authentication/authorization policies are out of scope (the spec does not define who is allowed to call each endpoint), but the Users API includes a login endpoint in scope that issues a Spring Security token when valid credentials are provided.
- The login endpoint returns a token representation as defined in the OpenAPI contract; token format (e.g., JWT vs opaque) is intentionally unspecified.
- The API treats `username` and `emailAddress` as identity/contact fields; uniqueness constraints apply to at least one of them (default assumption: `emailAddress` is unique).
- For list endpoints, pagination uses explicit query parameters defined in the OpenAPI contract and is required for the list users operation.
