# Tasks: User Management API System

**Input**: Design documents from `/specs/main/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.contract.yaml

**Spring-first development**: Tasks MUST use Spring Boot annotations and capabilities (dependency
injection, `@Valid` validation, `@ControllerAdvice` exception handling, Spring Data repositories,
Spring Security, `@Transactional`, etc.) rather than custom solutions.

**Tests**: Tests are MANDATORY. Tasks MUST include unit + integration tests for happy paths AND
negative/error flows (invalid inputs, error responses, exception paths), aligned with the OpenAPI
contract and HTTP status semantics.

**Feature flags (mandatory)**: Tasks MUST include implementing a Spring boolean property flag named
`FeatureFlag.usersApi` to gate the behavior (`true` enables, `false` disables), with default value
`false`, and tests for both enabled and disabled modes.

**Paged results (mandatory)**: List endpoints MUST include paged response shape + required pagination
inputs in OpenAPI, plus tests for paging boundaries (empty page, last page, max page size).

**Public API compatibility (mandatory)**: Tasks MUST NOT introduce explicit API versioning (no `/v1`,
headers, query params, or media type versioning). All changes must be backward compatible.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create Spring Boot project structure following standard Maven/Gradle conventions
- [ ] T002 Configure Gradle build file (build.gradle) with Spring Boot 3.5.9, Java 17, and required dependencies
- [ ] T003 [P] Add OpenAPI Generator Gradle plugin (7.14.0) configuration in build.gradle
- [ ] T004 [P] Copy OpenAPI contract from specs/main/contracts/openapi.contract.yaml to src/main/resources/openapi.yaml
- [ ] T005 [P] Create application.yml configuration file in src/main/resources/ with H2, Flyway, JWT, and server settings
- [ ] T006 [P] Create SpecDrivenApplication.java main class in src/main/java/com/example/specdriven/
- [ ] T007 Verify OpenAPI code generation runs successfully (./gradlew openApiGenerate)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database & Migrations

- [ ] T008 Create Flyway migration V001__init_schema.sql in src/main/resources/db/migration/ with users table and indexes
- [ ] T009 [P] Create Flyway migration V002__roles_permissions.sql with roles, permissions, role_permissions tables and seed data
- [ ] T010 [P] Create Flyway migration V003__users_api.sql with user_roles join table and indexes

### Domain Entities

- [ ] T011 [P] Create UserEntity.java in src/main/java/com/example/specdriven/domain/ with Spring Data JDBC annotations
- [ ] T012 [P] Create RoleEntity.java in src/main/java/com/example/specdriven/domain/ with Spring Data JDBC annotations
- [ ] T013 [P] Create PermissionEntity.java in src/main/java/com/example/specdriven/domain/ with Spring Data JDBC annotations
- [ ] T014 [P] Create UserRoleEntity.java in src/main/java/com/example/specdriven/domain/ with composite key
- [ ] T015 [P] Create RolePermissionEntity.java in src/main/java/com/example/specdriven/domain/ with composite key

### Repositories

- [ ] T016 [P] Create UserRepository.java interface in src/main/java/com/example/specdriven/repository/ extending CrudRepository
- [ ] T017 [P] Create UserRoleRepository.java interface in src/main/java/com/example/specdriven/repository/ extending CrudRepository
- [ ] T018 Add custom query methods to UserRepository for filtering (findByEmail, findByUsername, findAllPaginated)

### Security Infrastructure

- [ ] T019 Create JwtConfig.java in src/main/java/com/example/specdriven/config/ with JWT properties (secret, expirationMs)
- [ ] T020 Create JwtTokenProvider.java in src/main/java/com/example/specdriven/security/ for JWT generation and validation
- [ ] T021 Create JwtAuthenticationFilter.java in src/main/java/com/example/specdriven/security/ to intercept and validate bearer tokens
- [ ] T022 Create SecurityConfig.java in src/main/java/com/example/specdriven/config/ with Spring Security filter chain configuration
- [ ] T023 Configure BCryptPasswordEncoder bean in SecurityConfig.java

### Feature Flag Infrastructure

- [ ] T024 Create FeatureFlagConfig.java in src/main/java/com/example/specdriven/config/ with @ConfigurationProperties("FeatureFlag")
- [ ] T025 Create FeatureFlagService.java in src/main/java/com/example/specdriven/service/ to check feature flag state
- [ ] T026 Create FeatureFlagSecurityFilter.java in src/main/java/com/example/specdriven/security/ to enforce feature flags
- [ ] T027 Add FeatureFlag.usersApi property to application.yml with default value false

### Error Handling Infrastructure

- [ ] T028 Create custom exception classes in src/main/java/com/example/specdriven/exception/: ValidationException, ResourceNotFoundException, ConflictException
- [ ] T029 Create ErrorResponseFactory.java in src/main/java/com/example/specdriven/exception/ to generate stable error codes
- [ ] T030 Create GlobalExceptionHandler.java in src/main/java/com/example/specdriven/exception/ with @ControllerAdvice for consistent error responses

### Mapper Infrastructure

- [ ] T031 [P] Create UserMapper.java in src/main/java/com/example/specdriven/mapper/ for entity ↔ DTO conversions with password hashing
- [ ] T032 [P] Create RoleMapper.java in src/main/java/com/example/specdriven/mapper/ for entity ↔ DTO conversions

### Repository Tests

- [ ] T033 [P] Create UserRepositoryTest.java in src/test/java/com/example/specdriven/repository/ for CRUD operations
- [ ] T034 [P] Create test resources (application-test.yml) in src/test/resources/ with test configuration

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - System Health Monitoring (Priority: P1) 🎯 MVP

**Goal**: Provide a lightweight health check endpoint that requires no authentication for monitoring system availability

**Independent Test**: Call /ping endpoint and verify 200 response without authentication or database dependencies

### Tests for User Story 1 (MANDATORY)

- [ ] T035 [P] [US1] Create HealthCheckIntegrationTest.java in src/test/java/com/example/specdriven/integration/ for happy path (200 response)
- [ ] T036 [P] [US1] Add negative test to HealthCheckIntegrationTest.java for internal error scenario (500 response)
- [ ] T037 [P] [US1] Add performance test to HealthCheckIntegrationTest.java verifying <1s response time for 95% of requests
- [ ] T038 [P] [US1] Add feature flag test to HealthCheckIntegrationTest.java verifying /ping always available regardless of FeatureFlag.usersApi

### Implementation for User Story 1

- [ ] T039 [US1] Create PingController.java in src/main/java/com/example/specdriven/controller/ implementing generated PingApi interface
- [ ] T040 [US1] Configure SecurityConfig.java to permit /ping endpoint without authentication
- [ ] T041 [US1] Configure FeatureFlagSecurityFilter.java to explicitly bypass /ping endpoint

**Checkpoint**: Health check endpoint fully functional and independently testable

---

## Phase 4: User Story 2 - User Authentication (Priority: P1)

**Goal**: Enable users to authenticate with credentials and receive bearer tokens for accessing protected resources

**Independent Test**: Submit valid credentials to /login and verify token issuance; use token to access protected endpoint

### Tests for User Story 2 (MANDATORY)

- [ ] T042 [P] [US2] Create LoginIntegrationTest.java in src/test/java/com/example/specdriven/integration/ for valid credentials → 200 + token
- [ ] T043 [P] [US2] Add negative test for unknown username → 400 + non-enumerating error (same as wrong password)
- [ ] T044 [P] [US2] Add negative test for incorrect password → 400 + same error code/message as unknown username
- [ ] T045 [P] [US2] Add negative test for missing required fields → 400 + validation error
- [ ] T046 [P] [US2] Add negative test for malformed Authorization header → 401 + authentication error
- [ ] T047 [P] [US2] Add negative test for expired/invalid token → 401 + authentication error
- [ ] T048 [P] [US2] Add feature flag test verifying /login gated by FeatureFlag.usersApi (404 when disabled)
- [ ] T049 [P] [US2] Create LoginServiceTest.java in src/test/java/com/example/specdriven/service/ for credential validation logic
- [ ] T050 [P] [US2] Create JwtTokenProviderTest.java in src/test/java/com/example/specdriven/security/ for token generation/validation

### Implementation for User Story 2

- [ ] T051 [US2] Create LoginService.java in src/main/java/com/example/specdriven/service/ for credential validation and token generation
- [ ] T052 [US2] Create LoginController.java in src/main/java/com/example/specdriven/controller/ implementing generated LoginApi interface
- [ ] T053 [US2] Implement non-enumeration logic in LoginService.java (same error for unknown user and wrong password)
- [ ] T054 [US2] Implement timing-attack mitigation in LoginService.java (always hash password even if user not found)
- [ ] T055 [US2] Configure JWT claims in JwtTokenProvider.java (sub: userId, exp: expiration, iat: issued at)
- [ ] T056 [US2] Configure FeatureFlagSecurityFilter.java to gate /login endpoint with FeatureFlag.usersApi

**Checkpoint**: Authentication fully functional; tokens can be issued and validated

---

## Phase 5: User Story 3 - Create and Retrieve Users (Priority: P1)

**Goal**: Enable authenticated users to create new users and retrieve user details

**Independent Test**: Create user with valid data → 201; retrieve by ID → 200 with correct data; verify password not in response

### Tests for User Story 3 (MANDATORY)

- [ ] T057 [P] [US3] Create UserCrudIntegrationTest.java in src/test/java/com/example/specdriven/integration/ for create user happy path (201)
- [ ] T058 [P] [US3] Add test for retrieve user by ID happy path (200 with complete data)
- [ ] T059 [P] [US3] Add test verifying password field not included in response
- [ ] T060 [P] [US3] Add negative test for missing required fields → 400 + validation error
- [ ] T061 [P] [US3] Add negative test for invalid field formats → 400 + validation error
- [ ] T062 [P] [US3] Add negative test for duplicate email → 409 + conflict error
- [ ] T063 [P] [US3] Add negative test for non-existent user ID → 404 + not found error
- [ ] T064 [P] [US3] Add negative test for unauthenticated request → 401 + authentication error
- [ ] T065 [P] [US3] Add negative test for database failure → 503 + service unavailable with Retry-After header
- [ ] T066 [P] [US3] Add test for bootstrap mode (create first user without auth succeeds)
- [ ] T067 [P] [US3] Add test verifying bootstrap mode ends after first user (subsequent creates require auth)
- [ ] T068 [P] [US3] Add feature flag test verifying endpoints gated by FeatureFlag.usersApi
- [ ] T069 [P] [US3] Create UserServiceTest.java in src/test/java/com/example/specdriven/service/ for business logic
- [ ] T070 [P] [US3] Create UserMapperTest.java in src/test/java/com/example/specdriven/mapper/ for DTO ↔ entity conversions

### Implementation for User Story 3

- [ ] T071 [US3] Create UserService.java in src/main/java/com/example/specdriven/service/ with create and read logic
- [ ] T072 [US3] Implement email uniqueness validation in UserService.java (check before insert)
- [ ] T073 [US3] Implement bootstrap mode logic in UserService.java (user count check for conditional auth)
- [ ] T074 [US3] Create UsersController.java in src/main/java/com/example/specdriven/controller/ implementing generated UsersApi interface
- [ ] T075 [US3] Implement createUser endpoint in UsersController.java delegating to UserService
- [ ] T076 [US3] Implement getUserById endpoint in UsersController.java delegating to UserService
- [ ] T077 [US3] Implement password hashing in UserMapper.java (BCrypt before storing)
- [ ] T078 [US3] Ensure passwords never mapped to response DTOs in UserMapper.java
- [ ] T079 [US3] Add custom security logic in SecurityConfig.java for bootstrap mode (permit create user when zero users)
- [ ] T080 [US3] Configure FeatureFlagSecurityFilter.java to gate /users/* endpoints with FeatureFlag.usersApi

**Checkpoint**: User creation and retrieval fully functional; bootstrap mode working

---

## Phase 6: User Story 4 - Update and Delete Users (Priority: P1)

**Goal**: Enable authenticated users to update user information and delete users for lifecycle management

**Independent Test**: Create user → update fields → verify changes → delete → confirm removal (404)

### Tests for User Story 4 (MANDATORY)

- [ ] T081 [P] [US4] Add update user happy path test to UserCrudIntegrationTest.java (200 with updated data)
- [ ] T082 [P] [US4] Add delete user happy path test to UserCrudIntegrationTest.java (204 no content)
- [ ] T083 [P] [US4] Add test verifying deleted user returns 404 on subsequent retrieval
- [ ] T084 [P] [US4] Add negative test for update with invalid field values → 400 + validation error
- [ ] T085 [P] [US4] Add negative test for update non-existent user → 404 + not found error
- [ ] T086 [P] [US4] Add negative test for delete non-existent user → 404 + not found error
- [ ] T087 [P] [US4] Add negative test for update with conflicting email → 409 + conflict error
- [ ] T088 [P] [US4] Add negative test for unauthenticated update/delete → 401 + authentication error
- [ ] T089 [P] [US4] Add test for cascade delete (user deletion removes role assignments)
- [ ] T090 [P] [US4] Add unit tests for update logic to UserServiceTest.java

### Implementation for User Story 4

- [ ] T091 [US4] Add update method to UserService.java with validation and email uniqueness check
- [ ] T092 [US4] Add delete method to UserService.java with cascade handling
- [ ] T093 [US4] Implement updateUser endpoint in UsersController.java delegating to UserService
- [ ] T094 [US4] Implement deleteUser endpoint in UsersController.java delegating to UserService
- [ ] T095 [US4] Implement partial update logic in UserMapper.java (only update provided fields)
- [ ] T096 [US4] Update UserMapper.java to handle password hashing for updates (if password provided)
- [ ] T097 [US4] Ensure updated_at timestamp set in UserMapper.java on updates

**Checkpoint**: User update and delete fully functional; lifecycle management complete

---

## Phase 7: User Story 5 - List and Filter Users (Priority: P2)

**Goal**: Enable authenticated users to retrieve paginated lists of users with optional filtering for efficient browsing

**Independent Test**: Create multiple users → retrieve pages with various filters → verify pagination and filtering behavior

### Tests for User Story 5 (MANDATORY)

- [ ] T098 [P] [US5] Create UserListIntegrationTest.java in src/test/java/com/example/specdriven/integration/ for paginated list (200 + metadata)
- [ ] T099 [P] [US5] Add test for multiple pages (verify page navigation and totalPages calculation)
- [ ] T100 [P] [US5] Add test for filtering by username (exact match)
- [ ] T101 [P] [US5] Add test for filtering by email (exact match)
- [ ] T102 [P] [US5] Add test for filtering by name (case-insensitive partial match)
- [ ] T103 [P] [US5] Add test for filtering by role (users with specific role)
- [ ] T104 [P] [US5] Add test for multiple filters applied with AND logic
- [ ] T105 [P] [US5] Add test for empty results with valid pagination metadata
- [ ] T106 [P] [US5] Add negative test for missing pagination parameters → 400 + validation error
- [ ] T107 [P] [US5] Add negative test for invalid pagination parameters (negative page, zero/excessive pageSize) → 400
- [ ] T108 [P] [US5] Add negative test for unsupported query parameters → 400 + validation error
- [ ] T109 [P] [US5] Add test for maximum page size enforcement (100 items)
- [ ] T110 [P] [US5] Add negative test for unauthenticated request → 401
- [ ] T111 [P] [US5] Add pagination boundary tests to UserServiceTest.java (last page, empty results)

### Implementation for User Story 5

- [ ] T112 [US5] Add listUsers method to UserService.java with pagination and filtering logic
- [ ] T113 [US5] Add custom query methods to UserRepository.java for paginated queries with LIMIT/OFFSET
- [ ] T114 [US5] Add filtering methods to UserRepository.java (by username, email, name, role)
- [ ] T115 [US5] Implement listUsers endpoint in UsersController.java delegating to UserService
- [ ] T116 [US5] Implement pagination metadata calculation in UserService.java (totalPages = ceil(totalCount / pageSize))
- [ ] T117 [US5] Implement role filtering logic in UserService.java (join with user_roles table)
- [ ] T118 [US5] Add validation for pagination parameters in UsersController.java (@Valid on parameters)
- [ ] T119 [US5] Implement UserPage response mapping in UserMapper.java

**Checkpoint**: User listing and filtering fully functional; pagination working correctly

---

## Phase 8: User Story 6 - Manage User Roles (Priority: P2)

**Goal**: Enable authenticated users to assign and remove roles from users for access control

**Independent Test**: Assign role to user → verify in user representation → remove role → confirm removal

### Tests for User Story 6 (MANDATORY)

- [ ] T120 [P] [US6] Create RoleManagementIntegrationTest.java in src/test/java/com/example/specdriven/integration/ for assign role (204)
- [ ] T121 [P] [US6] Add test for remove role (204)
- [ ] T122 [P] [US6] Add test verifying role appears in user representation after assignment
- [ ] T123 [P] [US6] Add test verifying role removed from user representation after removal
- [ ] T124 [P] [US6] Add test for idempotent assign (assign already-assigned role → 204, no duplicate)
- [ ] T125 [P] [US6] Add test for idempotent remove (remove non-assigned role → 204, no error)
- [ ] T126 [P] [US6] Add negative test for assign role to non-existent user → 404
- [ ] T127 [P] [US6] Add negative test for invalid/unknown role name → 400 + validation error
- [ ] T128 [P] [US6] Add negative test for unauthenticated request → 401
- [ ] T129 [P] [US6] Create RoleServiceTest.java in src/test/java/com/example/specdriven/service/ for role logic

### Implementation for User Story 6

- [ ] T130 [US6] Create RoleService.java in src/main/java/com/example/specdriven/service/ with assign and remove logic
- [ ] T131 [US6] Implement idempotent assign role logic in RoleService.java (check if exists before insert)
- [ ] T132 [US6] Implement idempotent remove role logic in RoleService.java (check if exists before delete)
- [ ] T133 [US6] Add methods to UserRoleRepository.java for role assignment operations
- [ ] T134 [US6] Create UserRolesController.java in src/main/java/com/example/specdriven/controller/ implementing generated APIs
- [ ] T135 [US6] Implement assignRole endpoint in UserRolesController.java delegating to RoleService
- [ ] T136 [US6] Implement removeRole endpoint in UserRolesController.java delegating to RoleService
- [ ] T137 [US6] Update UserService.java to load roles when retrieving users (JOIN with user_roles and roles tables)

**Checkpoint**: Role management fully functional; idempotent operations working

---

## Phase 9: Cross-Cutting Concerns & Final Integration

**Purpose**: Error handling, retry behavior, feature flags, and comprehensive testing

### Error Handling & Retry Semantics

- [ ] T138 [P] Create error consistency tests verifying ErrorResponse structure (code + message fields) across all endpoints
- [ ] T139 [P] Add tests for stable error codes: VALIDATION_FAILED (400), RESOURCE_NOT_FOUND (404), CONFLICT (409)
- [ ] T140 [P] Add tests for AUTHENTICATION_REQUIRED/FAILED (401), INTERNAL_ERROR (500), SERVICE_UNAVAILABLE (503)
- [ ] T141 [P] Add test verifying 503 responses include Retry-After header when appropriate
- [ ] T142 [P] Verify no error responses contain custom 'retryable' field
- [ ] T143 [P] Create GlobalExceptionHandlerTest.java in src/test/java/com/example/specdriven/exception/ for exception mapping

### Feature Flag Validation

- [ ] T144 [P] Create FeatureFlagIntegrationTest.java in src/test/java/com/example/specdriven/integration/ for flag disabled → 404
- [ ] T145 [P] Add test for flag enabled → endpoints process requests normally
- [ ] T146 [P] Add test verifying /ping always available regardless of feature flag
- [ ] T147 [P] Add test verifying error messages don't reveal feature exists when disabled
- [ ] T148 [P] Verify all /users/* and /login endpoints gated by FeatureFlag.usersApi

### Integration & End-to-End Testing

- [ ] T149 [P] Add full user lifecycle integration test (create → assign role → update → list → delete)
- [ ] T150 [P] Add authentication flow integration test (bootstrap user → login → create second user → verify auth required)
- [ ] T151 [P] Add pagination integration test with large dataset (100+ users across multiple pages)
- [ ] T152 [P] Add concurrent update integration test (multiple simultaneous updates to same user)
- [ ] T153 [P] Add feature flag toggle integration test (disable → verify 404 → enable → verify functional)

### Performance Testing

- [ ] T154 [P] Add performance smoke tests for health check (<1s for 95% requests)
- [ ] T155 [P] Add performance smoke tests for login operation (<1s for 95% requests)
- [ ] T156 [P] Add performance smoke tests for CRUD operations (<1s for 95% requests)
- [ ] T157 [P] Add performance smoke tests for paginated list (<1s for 95% requests)

---

## Phase 10: Documentation & Validation

**Purpose**: Documentation updates and final validation

- [ ] T158 [P] Update README.md with API usage examples and quickstart instructions
- [ ] T159 [P] Verify quickstart.md scenarios work end-to-end
- [ ] T160 [P] Generate API documentation from OpenAPI contract (Swagger UI or similar)
- [ ] T161 [P] Create developer setup guide in docs/
- [ ] T162 [P] Document environment variable requirements for production (JWT_SECRET)
- [ ] T163 [P] Add code comments for complex logic (bootstrap mode, non-enumeration, idempotent operations)
- [ ] T164 Run complete build: ./gradlew clean build (all tests must pass)
- [ ] T165 Verify application starts successfully: ./gradlew bootRun
- [ ] T166 Manual smoke test of all endpoints using curl or Postman
- [ ] T167 Generate test coverage report and verify adequate coverage

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) completion - BLOCKS all user stories
- **User Stories (Phase 3-8)**: All depend on Foundational (Phase 2) completion
  - User stories can proceed in parallel if staffed
  - Or sequentially in priority order: US1-4 (P1) → US5-6 (P2)
- **Cross-Cutting (Phase 9)**: Depends on all user stories being complete
- **Documentation (Phase 10)**: Depends on cross-cutting tests completion

### User Story Dependencies

- **US1 (Health Check)**: Independent - can start after Foundational
- **US2 (Authentication)**: Independent - can start after Foundational
- **US3 (Create/Retrieve Users)**: Depends on US2 (needs authentication)
- **US4 (Update/Delete Users)**: Depends on US3 (needs users to exist)
- **US5 (List/Filter Users)**: Depends on US3 (needs users to exist)
- **US6 (Manage Roles)**: Depends on US3 (needs users to exist)

### Within Each User Story

- Tests can be written in parallel (all marked [P])
- Models before services
- Services before controllers
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] run in parallel
- All Foundational tasks marked [P] run in parallel (within Phase 2 groups)
- After Foundational: US1, US2 can start in parallel
- Tests within each user story run in parallel
- Different user stories can be worked on simultaneously by different developers

---

## Implementation Strategy

### MVP First (User Stories 1-4 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Health Check)
4. Complete Phase 4: User Story 2 (Authentication)
5. Complete Phase 5: User Story 3 (Create/Retrieve Users)
6. Complete Phase 6: User Story 4 (Update/Delete Users)
7. **STOP and VALIDATE**: Test all stories independently
8. Run build and verify all tests pass
9. Deploy/demo MVP

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add US1 (Health Check) → Test independently → Deploy/Demo
3. Add US2 (Authentication) → Test independently → Deploy/Demo
4. Add US3 (Create/Retrieve) → Test independently → Deploy/Demo (MVP!)
5. Add US4 (Update/Delete) → Test independently → Deploy/Demo
6. Add US5 (List/Filter) → Test independently → Deploy/Demo
7. Add US6 (Manage Roles) → Test independently → Deploy/Demo
8. Complete cross-cutting concerns → Full system validated
9. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers after Foundational phase completes:

- **Developer A**: User Story 1 + User Story 2
- **Developer B**: User Story 3 + User Story 4
- **Developer C**: User Story 5 + User Story 6
- **All together**: Cross-cutting concerns and integration testing

---

## Summary

**Total Tasks**: 167
**Task Breakdown**:
- Phase 1 (Setup): 7 tasks
- Phase 2 (Foundational): 27 tasks
- Phase 3 (US1 - Health Check): 7 tasks
- Phase 4 (US2 - Authentication): 15 tasks
- Phase 5 (US3 - Create/Retrieve): 24 tasks
- Phase 6 (US4 - Update/Delete): 17 tasks
- Phase 7 (US5 - List/Filter): 22 tasks
- Phase 8 (US6 - Manage Roles): 18 tasks
- Phase 9 (Cross-Cutting): 20 tasks
- Phase 10 (Documentation): 10 tasks

**Parallel Opportunities**: 118 tasks can run in parallel with other tasks in same phase

**Independent Test Criteria**:
- US1: Call /ping without auth → 200
- US2: Login with credentials → receive token → use token
- US3: Create user → retrieve by ID → verify data
- US4: Update user → delete user → verify removal
- US5: Create multiple users → paginate → filter → verify results
- US6: Assign role → verify in user → remove role → verify removal

**MVP Scope**: User Stories 1-4 (Health Check + Authentication + User CRUD)

**Build Gate**: `./gradlew clean build` must pass (all tests, OpenAPI generation, compilation)

---

**Tasks Status**: ✅ Complete
**Last Updated**: 2025-12-30
**Next Action**: Begin implementation starting with Phase 1 (Setup)
