---

description: "Task list for implementing Users API (Users & Roles) with AuthN/AuthZ"
---

# Tasks: Users API (Users & Roles)  Authentication/Authorization

**Input**: Design documents from `specs/003-users-api/` (`plan.md`, `spec.md`, `research.md`, `data-model.md`, `quickstart.md`, `contracts/openapi.contract.yaml`)

**Tests**: Tests are **MANDATORY** for this feature (per plan/spec/quickstart). Include happy-path + negative/error flows and validate HTTP semantics + shared `ErrorResponse`.

**Feature flag (mandatory)**: All Users API endpoints, including `/login`, must be gated by `FeatureFlag.usersApi` (default `false`). Disabled behavior must be **404** with shared error body.

**Auth (mandatory)**:
- Spring Security Bearer tokens
- `/login` public (no bearer)
- Bootstrap exception: unauthenticated `POST /users` allowed **only** when user count == 0
- All other Users endpoints require bearer token
- Protected endpoints return **401** (missing/invalid token) and **403** (valid token but forbidden) with shared error body

**OpenAPI-first (mandatory)**: Update `src/main/resources/openapi.yaml` to match `specs/003-users-api/contracts/openapi.contract.yaml`, then regenerate `src/main-gen/java`.

---

## Phase 1: Setup (Shared Infrastructure)

- [x] T001 Verify current build is green (`./gradlew test`) and note baseline failures (repo root)
- [x] T002 Sync local feature docs into agent context (run `.specify/scripts/powershell/update-agent-context.ps1 -AgentType copilot`) (`.specify/scripts/powershell/update-agent-context.ps1`)

---

## Phase 2: Foundational (Blocking Prerequisites)

** CRITICAL**: No user story work should start until this phase is complete.

### OpenAPI source-of-truth + code generation

- [x] T003 Update Users API contract in `src/main/resources/openapi.yaml` to match `specs/003-users-api/contracts/openapi.contract.yaml` (paths/schemas/security/401/403/bootstrap docs)
- [x] T004 Regenerate OpenAPI interfaces/models (`openApiGenerate`) and ensure generated sources compile (`build.gradle`, `src/main-gen/java/...`)

### Feature-flag wiring (404 when disabled)

- [x] T005 Implement `FeatureFlag.usersApi` property wiring (default `false`) (`src/main/java/com/example/specdriven/config/FeatureFlagProperties.java`, `src/main/resources/application.properties`)
- [x] T006 [P] Add reusable controller/handler helper to short-circuit disabled endpoints to 404 with shared error (`src/main/java/com/example/specdriven/feature/UsersApiFeatureGate.java`)
- [x] T007 [P] Add tests that Users endpoints (including `/login`) return 404 when `FeatureFlag.usersApi=false` (`src/test/java/com/example/specdriven/users/UsersApiFeatureFlagDisabledTest.java`)

### Persistence foundation (SQL + H2 default)

- [x] T008 Add persistence dependencies and baseline configuration (H2 + Spring Data JDBC) (`build.gradle`, `src/main/resources/application.properties`)
- [x] T009 Create DB schema for users/roles/user_roles/role_permissions (Flyway or schema.sql; pick repo convention) (`src/main/resources/db/migration/V003__users_api.sql` or `src/main/resources/schema.sql`)
- [x] T010 Implement repositories for users and role relations (Data JDBC) (`src/main/java/com/example/specdriven/users/persistence/UserEntity.java`, `src/main/java/com/example/specdriven/users/persistence/UserRepository.java`, `src/main/java/com/example/specdriven/users/persistence/UserRoleRepository.java`)
- [x] T011 [P] Add persistence smoke test verifying schema + basic insert/select works on H2 (`src/test/java/com/example/specdriven/users/persistence/UserRepositoryTest.java`)

### Error codes + shared error mapping for Users feature

- [x] T012 Extend `ApiErrorCode` with Users/auth-specific stable codes (UNAUTHORIZED, FORBIDDEN, USER_NOT_FOUND, RESOURCE_CONFLICT, INVALID_CREDENTIALS, FEATURE_DISABLED as needed) (`src/main/java/com/example/specdriven/error/ApiErrorCode.java`)
- [x] T013 Map Spring Security auth failures to shared `ErrorResponse` with correct code/status (401/403) (`src/main/java/com/example/specdriven/error/GlobalExceptionHandler.java`, `src/main/java/com/example/specdriven/security/SecurityExceptionHandlers.java`)
- [ ] T014 [P] Add tests for 401/403 error responses body shape + code mapping (`src/test/java/com/example/specdriven/security/SecurityErrorResponsesTest.java`)

### Spring Security bearer tokens + bootstrap rule infrastructure

- [x] T015 Add Spring Security configuration for stateless bearer auth + public `/login` route (`src/main/java/com/example/specdriven/security/SecurityConfig.java`)
- [x] T016 Implement token service (mint + validate) (JWT or opaque; keep internal) (`src/main/java/com/example/specdriven/security/TokenService.java`)
- [x] T017 Implement bearer token authentication filter that sets authenticated principal on valid token (`src/main/java/com/example/specdriven/security/BearerTokenAuthFilter.java`)
- [x] T018 Implement bootstrap authorization rule: allow unauthenticated `POST /users` only when `UserRepository.count()==0` (`src/main/java/com/example/specdriven/security/BootstrapCreateUserAuthorizationManager.java`)
- [ ] T019 [P] Add security integration tests: `/login` public, protected endpoints require bearer, bootstrap create-user behavior (0 users  allow, >0 users deny) (`src/test/java/com/example/specdriven/security/UsersApiSecurityRulesTest.java`)

**Checkpoint**: OpenAPI regenerated, feature flag exists + tested, persistence works, security infra exists + tested.

---

## Phase 3: User Story 1  Create and view a user (Priority: P1)  MVP

**Goal**: Create a user and retrieve it by ID, returning contract-compliant responses and shared errors.

**Independent Test**: With flag enabled, create (201) then get-by-id (200). Validate 400/404/409/503 and auth (401) behaviors.

### Tests (MANDATORY)

- [ ] T020 [P] [US1] Integration test: bootstrap create user (unauthenticated)  201 and returns User with roles (`src/test/java/com/example/specdriven/users/CreateAndGetUserTest.java`)
- [ ] T021 [P] [US1] Integration test: get user by id  200 after create (`src/test/java/com/example/specdriven/users/CreateAndGetUserTest.java`)
- [ ] T022 [P] [US1] Negative: create user invalid body/unknown fields  400 + shared error code (`src/test/java/com/example/specdriven/users/CreateUserValidationTest.java`)
- [ ] T023 [P] [US1] Negative: get missing user  404 + shared error code (`src/test/java/com/example/specdriven/users/GetUserNotFoundTest.java`)
- [ ] T024 [P] [US1] Negative: create duplicate email  409 + shared error code (`src/test/java/com/example/specdriven/users/CreateUserConflictTest.java`)

### Implementation

- [ ] T025 [P] [US1] Implement domain model + mappers between generated OpenAPI DTOs and persistence entities (`src/main/java/com/example/specdriven/users/UsersMapper.java`)
- [ ] T026 [P] [US1] Implement password hashing (BCrypt) and ensure password is never returned (`src/main/java/com/example/specdriven/users/security/PasswordHasher.java`)
- [ ] T027 [US1] Implement `UsersService.createUser` with uniqueness handling (409) and default roles (if any) (`src/main/java/com/example/specdriven/users/UsersService.java`)
- [ ] T028 [US1] Implement `UsersService.getUserById` (404 when missing) (`src/main/java/com/example/specdriven/users/UsersService.java`)
- [ ] T029 [US1] Implement generated API interface for `POST /users` and `GET /users/{userId}` with feature-flag gate (`src/main/java/com/example/specdriven/users/UsersController.java`)

**Checkpoint**: US1 passes all tests and is usable end-to-end (bootstrap create + get).

---

## Phase 4: User Story 5  Login and obtain an authentication token (Priority: P1)

**Goal**: Exchange username/password for bearer token without leaking user existence.

**Independent Test**: Create a user with known password, login returns `200 {token}`, then use token to call a protected endpoint.

### Tests (MANDATORY)

- [ ] T030 [P] [US5] Integration test: login success returns non-empty token (`src/test/java/com/example/specdriven/users/LoginTest.java`)
- [ ] T031 [P] [US5] Integration test: token enables calling protected endpoint (e.g., `GET /users/{id}`) (`src/test/java/com/example/specdriven/users/LoginTest.java`)
- [ ] T032 [P] [US5] Negative: missing username/password  400 + shared error code (`src/test/java/com/example/specdriven/users/LoginValidationTest.java`)
- [ ] T033 [P] [US5] Negative: unknown username vs wrong password are indistinguishable  400 + same error code (`src/test/java/com/example/specdriven/users/LoginInvalidCredentialsTest.java`)

### Implementation

- [ ] T034 [P] [US5] Implement login request validation mapping into shared errors (`src/main/java/com/example/specdriven/users/LoginController.java`)
- [ ] T035 [US5] Implement credential verification: lookup by username, verify password hash, mint token (`src/main/java/com/example/specdriven/users/LoginService.java`)
- [ ] T036 [US5] Wire `POST /login` generated API interface to `LoginService` and feature-flag gate (`src/main/java/com/example/specdriven/users/LoginController.java`)

**Checkpoint**: US5 tests pass; clients can obtain token and call protected endpoints.

---

## Phase 5: User Story 2  Update and delete a user (Priority: P1)

**Goal**: Update mutable fields and delete users with correct HTTP semantics and shared errors.

**Independent Test**: Create  login  update  get  delete  get returns 404.

### Tests (MANDATORY)

- [ ] T037 [P] [US2] Integration test: update user happy path  200 and changes persisted (`src/test/java/com/example/specdriven/users/UpdateUserTest.java`)
- [ ] T038 [P] [US2] Integration test: delete user happy path  204 and subsequent get returns 404 (`src/test/java/com/example/specdriven/users/DeleteUserTest.java`)
- [ ] T039 [P] [US2] Negative: update invalid body/unknown fields/attempt change `id`  400 (`src/test/java/com/example/specdriven/users/UpdateUserValidationTest.java`)
- [ ] T040 [P] [US2] Negative: update/delete missing user  404 (`src/test/java/com/example/specdriven/users/UserNotFoundMutationTest.java`)
- [ ] T041 [P] [US2] Negative: update causes email conflict  409 (`src/test/java/com/example/specdriven/users/UpdateUserConflictTest.java`)

### Implementation

- [ ] T042 [US2] Implement `UsersService.updateUser` with immutable-field enforcement + optional password update hashing (`src/main/java/com/example/specdriven/users/UsersService.java`)
- [ ] T043 [US2] Implement `UsersService.deleteUser` (idempotency choice per contract; return 404 when missing) (`src/main/java/com/example/specdriven/users/UsersService.java`)
- [ ] T044 [US2] Implement generated API interface handlers for `PUT /users/{userId}` and `DELETE /users/{userId}` with feature-flag gate (`src/main/java/com/example/specdriven/users/UsersController.java`)

---

## Phase 6: User Story 3  List and filter users (Priority: P2)

**Goal**: Paged listing with required `page`/`pageSize` and explicit filters, rejecting unknown query params.

**Independent Test**: Create many users, list with paging boundaries, and ensure filters work (AND semantics) and return paged metadata.

### Tests (MANDATORY)

- [ ] T045 [P] [US3] Integration test: list users requires pagination params  missing  400 (`src/test/java/com/example/specdriven/users/ListUsersValidationTest.java`)
- [ ] T046 [P] [US3] Integration test: list users paging boundaries (empty page, last page, max pageSize=100) (`src/test/java/com/example/specdriven/users/ListUsersPagingTest.java`)
- [ ] T047 [P] [US3] Integration test: list users filters (username/email exact, name contains, roleName exact) with AND behavior (`src/test/java/com/example/specdriven/users/ListUsersFilteringTest.java`)
- [ ] T048 [P] [US3] Negative: unknown query params  400 (`src/test/java/com/example/specdriven/users/ListUsersValidationTest.java`)

### Implementation

- [ ] T049 [US3] Implement repository queries for paging + filters (Data JDBC custom queries) (`src/main/java/com/example/specdriven/users/persistence/UserQueries.java`)
- [ ] T050 [US3] Implement `UsersService.listUsers` returning `UserPage` with totals (`src/main/java/com/example/specdriven/users/UsersService.java`)
- [ ] T051 [US3] Implement generated API interface handler for `GET /users` (`src/main/java/com/example/specdriven/users/UsersController.java`)

---

## Phase 7: User Story 4  Assign and remove roles for a user (Priority: P2)

**Goal**: Idempotent role assignment/removal, validating role values against contract-defined enum.

**Independent Test**: Create user  login  assign role  get user shows role  remove role  get user no longer shows role.

### Tests (MANDATORY)

- [ ] T052 [P] [US4] Integration test: assign role idempotent  204 and no duplicates (`src/test/java/com/example/specdriven/users/UserRolesTest.java`)
- [ ] T053 [P] [US4] Integration test: remove role idempotent  204 even when not assigned (`src/test/java/com/example/specdriven/users/UserRolesTest.java`)
- [ ] T054 [P] [US4] Negative: unknown role  400 (`src/test/java/com/example/specdriven/users/UserRolesValidationTest.java`)
- [ ] T055 [P] [US4] Negative: missing user  404 (`src/test/java/com/example/specdriven/users/UserRolesNotFoundTest.java`)

### Implementation

- [ ] T056 [P] [US4] Implement role catalog (enum mapping from generated RoleName/Permission) and default permission set per role (`src/main/java/com/example/specdriven/users/roles/RoleCatalog.java`)
- [ ] T057 [US4] Implement `UsersService.assignRoleToUser` and `removeRoleFromUser` with idempotency and validation (`src/main/java/com/example/specdriven/users/UsersService.java`)
- [ ] T058 [US4] Implement generated API interface handlers for role endpoints (`src/main/java/com/example/specdriven/users/UsersController.java`)

---

## Phase 8: Polish & Cross-Cutting Concerns

- [ ] T059 [P] Add OpenAPI contract drift guard: compare `specs/003-users-api/contracts/openapi.contract.yaml` with `src/main/resources/openapi.yaml` in CI/test (lightweight test or build check) (`src/test/java/com/example/specdriven/openapi/OpenApiContractParityTest.java`)
- [ ] T060 [P] Add quickstart smoke test script or README snippet for local manual verification (`specs/003-users-api/quickstart.md`)
- [ ] T061 Ensure `./gradlew test` passes and update any flaky tests (repo root)

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1  Phase 2  (Phase 3/4/5/6/7 in priority order):
  - **Foundational (Phase 2)** blocks all user stories.
  - User stories may be developed in parallel after Phase 2, but **P1** stories should ship first.

### User Story Dependencies

- **US1 (Create/Get user)**: depends on Phase 2 (OpenAPI generated + feature flag + persistence + security infrastructure). Can ship as MVP.
- **US5 (Login)**: depends on US1 (needs a user to exist to validate credentials) + Phase 2.
- **US2 (Update/Delete)**: depends on US1 + US5 (auth required for non-bootstrap operations).
- **US3 (List/Filter)**: depends on US1 + US5 (auth required; users must exist to list).
- **US4 (Role assignment)**: depends on US1 + US5 (auth required) and on role catalog foundation.

---

## Parallel Execution Examples (after Phase 2)

### US1 (Create/Get)
- T020/T021/T022/T023/T024 tests can be written in parallel.
- T025 (mappers) and T026 (password hashing) can be done in parallel.

### US5 (Login)
- T030T033 tests can be written in parallel.
- T034 (controller validation) and T035 (service) can be done in parallel once DTOs exist.

### US3 (List/Filter)
- T045T048 tests can be written in parallel.
- T049 query layer can proceed in parallel with test authoring.

---

## Implementation Strategy

### Suggested MVP Scope

Ship **US1 only** first (create + get) with:
- Feature flag gating (404 when disabled)
- Bootstrap unauthenticated create-user only when zero users
- Shared error semantics for 400/404/409/503 and auth 401/403

Then add **US5 (login)** so consumers can obtain tokens, and proceed with US2/US3/US4.

