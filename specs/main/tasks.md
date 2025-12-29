# Tasks: User Management API System

**Input**: Design documents from `/specs/main/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.contract.yaml

**Tests**: Tests are MANDATORY. Tasks MUST include unit + integration tests for happy paths AND negative/error flows (invalid inputs, error responses, exception paths), aligned with the OpenAPI contract and HTTP status semantics.

**Feature flags (mandatory)**: Tasks MUST include implementing a Spring boolean property flag named `FeatureFlag.usersApi` to gate the behavior (`true` enables, `false` disables, default: `false`), and adding tests for both enabled and disabled modes.

**Paged results (mandatory)**: List users endpoint MUST include paged response shape + required pagination inputs in OpenAPI, plus tests for paging boundaries.

**Public API compatibility (mandatory)**: Tasks MUST NOT introduce explicit API versioning. Only additive changes allowed.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Initialize Spring Boot project structure and build configuration

- [ ] T001 Initialize Spring Boot 3.5.9 project with JDK 17 in build.gradle
- [ ] T002 Configure Gradle OpenAPI Generator plugin in build.gradle for generating API interfaces from openapi.yaml
- [ ] T003 [P] Create package structure: com.example.specdriven with subdirectories for config, controller, service, repository, domain, mapper, exception, security
- [ ] T004 [P] Copy OpenAPI contract from specs/main/contracts/openapi.contract.yaml to src/main/resources/openapi.yaml
- [ ] T005 [P] Create application.yml in src/main/resources with database, server, and JWT configuration
- [ ] T006 [P] Create application-test.yml in src/test/resources with test-specific configuration
- [ ] T007 Add Spring Boot dependencies in build.gradle: spring-boot-starter-web, spring-boot-starter-data-jdbc, spring-boot-starter-security, spring-boot-starter-validation, spring-boot-starter-test
- [ ] T008 [P] Add additional dependencies in build.gradle: h2 database, flyway-core, jjwt-api, jjwt-impl, jjwt-jackson
- [ ] T009 [P] Configure build.gradle to generate OpenAPI code into src/main-gen/java before compilation
- [ ] T010 Create SpecDrivenApplication.java main class in src/main/java/com/example/specdriven

**Checkpoint**: Project builds successfully with `./gradlew clean build` and generates OpenAPI code

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database Schema & Migrations

- [ ] T011 Create Flyway migration V001__init_schema.sql in src/main/resources/db/migration for users table with indexes
- [ ] T012 [P] Create Flyway migration V002__roles_permissions.sql in src/main/resources/db/migration for roles, permissions, and role_permissions tables with seed data
- [ ] T013 [P] Create Flyway migration V003__users_api.sql in src/main/resources/db/migration for user_roles table with indexes

### Domain Entities

- [ ] T014 [P] Create UserEntity.java in src/main/java/com/example/specdriven/domain with Spring Data JDBC annotations
- [ ] T015 [P] Create RoleEntity.java in src/main/java/com/example/specdriven/domain with Spring Data JDBC annotations
- [ ] T016 [P] Create PermissionEntity.java in src/main/java/com/example/specdriven/domain with Spring Data JDBC annotations
- [ ] T017 [P] Create UserRoleEntity.java in src/main/java/com/example/specdriven/domain with composite key
- [ ] T018 [P] Create RolePermissionEntity.java in src/main/java/com/example/specdriven/domain with composite key

### Repositories

- [ ] T019 [P] Create UserRepository.java interface in src/main/java/com/example/specdriven/repository extending CrudRepository with custom query methods
- [ ] T020 [P] Create UserRoleRepository.java interface in src/main/java/com/example/specdriven/repository extending CrudRepository

### Security Configuration

- [ ] T021 Create JwtConfig.java in src/main/java/com/example/specdriven/config with JWT secret and expiration properties
- [ ] T022 [P] Create FeatureFlagConfig.java in src/main/java/com/example/specdriven/config with @ConfigurationProperties("FeatureFlag") and usersApi boolean property (default: false)
- [ ] T023 [P] Create JwtTokenProvider.java in src/main/java/com/example/specdriven/security for JWT generation and validation using jjwt library
- [ ] T024 Create JwtAuthenticationFilter.java in src/main/java/com/example/specdriven/security extending OncePerRequestFilter for bearer token validation
- [ ] T025 Create FeatureFlagSecurityFilter.java in src/main/java/com/example/specdriven/security to check FeatureFlag.usersApi and return 404 when disabled
- [ ] T026 Create SecurityConfig.java in src/main/java/com/example/specdriven/config with Spring Security filter chain configuration

### Exception Handling

- [ ] T027 [P] Create ValidationException.java in src/main/java/com/example/specdriven/exception
- [ ] T028 [P] Create ResourceNotFoundException.java in src/main/java/com/example/specdriven/exception
- [ ] T029 [P] Create ConflictException.java in src/main/java/com/example/specdriven/exception
- [ ] T030 [P] Create AuthenticationException.java in src/main/java/com/example/specdriven/exception
- [ ] T031 Create ErrorResponseFactory.java in src/main/java/com/example/specdriven/exception for generating stable error codes
- [ ] T032 Create GlobalExceptionHandler.java in src/main/java/com/example/specdriven/exception with @ControllerAdvice and exception-to-HTTP-status mapping

### Mappers

- [ ] T033 [P] Create UserMapper.java in src/main/java/com/example/specdriven/mapper for User DTO ↔ UserEntity conversion with password hashing
- [ ] T034 [P] Create RoleMapper.java in src/main/java/com/example/specdriven/mapper for Role DTO ↔ RoleEntity conversion

### Services

- [ ] T035 Create FeatureFlagService.java in src/main/java/com/example/specdriven/service with method to check if usersApi flag is enabled

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - System Health Monitoring (Priority: P1) 🎯 MVP

**Goal**: Provide a lightweight health check endpoint that requires no authentication for monitoring system availability

**Independent Test**: Call /ping endpoint without authentication and verify 200 response with "pong" message

### Tests for User Story 1 (MANDATORY)

- [ ] T036 [P] [US1] Create HealthCheckIntegrationTest.java in src/test/java/com/example/specdriven/integration
- [ ] T037 [P] [US1] Write test: ping endpoint returns 200 with PingResponse containing message "pong"
- [ ] T038 [P] [US1] Write test: ping endpoint responds in under 1 second (95th percentile)
- [ ] T039 [P] [US1] Write test: ping endpoint returns 500 with ErrorResponse when unexpected error occurs
- [ ] T040 [P] [US1] Write test: ping endpoint works regardless of FeatureFlag.usersApi value (not gated)

### Implementation for User Story 1

- [ ] T041 [US1] Create PingController.java in src/main/java/com/example/specdriven/controller implementing generated PingApi interface
- [ ] T042 [US1] Implement ping() method returning ResponseEntity with PingResponse("pong")
- [ ] T043 [US1] Verify SecurityConfig explicitly bypasses security for /ping endpoint

**Checkpoint**: Health check endpoint fully functional and testable independently

---

## Phase 4: User Story 2 - User Authentication (Priority: P1)

**Goal**: Enable API consumers to authenticate with credentials and receive JWT bearer tokens for accessing protected resources

**Independent Test**: Submit valid credentials to /login and verify token issuance; use token to access protected endpoint

### Tests for User Story 2 (MANDATORY)

- [ ] T044 [P] [US2] Create LoginIntegrationTest.java in src/test/java/com/example/specdriven/integration
- [ ] T045 [P] [US2] Write test: login with valid credentials returns 200 with JWT token
- [ ] T046 [P] [US2] Write test: valid bearer token authenticates protected request successfully
- [ ] T047 [P] [US2] Write test: login completes in under 1 second (95th percentile)
- [ ] T048 [P] [US2] Write test: login with unknown username returns 400 with AUTHENTICATION_FAILED error (non-enumerating)
- [ ] T049 [P] [US2] Write test: login with incorrect password returns 400 with same error as unknown username
- [ ] T050 [P] [US2] Write test: login with missing fields returns 400 with VALIDATION_FAILED error
- [ ] T051 [P] [US2] Write test: malformed Authorization header returns 401 with AUTHENTICATION_REQUIRED error
- [ ] T052 [P] [US2] Write test: expired or invalid bearer token returns 401 with AUTHENTICATION_FAILED error
- [ ] T053 [P] [US2] Write test: login endpoint gated by FeatureFlag.usersApi (returns 404 when disabled)

### Unit Tests for User Story 2

- [ ] T054 [P] [US2] Create LoginServiceTest.java in src/test/java/com/example/specdriven/service
- [ ] T055 [P] [US2] Create JwtTokenProviderTest.java in src/test/java/com/example/specdriven/security

### Implementation for User Story 2

- [ ] T056 [P] [US2] Create LoginService.java in src/main/java/com/example/specdriven/service with credential validation and token generation logic
- [ ] T057 [US2] Create LoginController.java in src/main/java/com/example/specdriven/controller implementing generated LoginApi interface
- [ ] T058 [US2] Implement login() method delegating to LoginService with error handling
- [ ] T059 [US2] Update JwtAuthenticationFilter to extract token from Authorization header and set Spring Security authentication context
- [ ] T060 [US2] Update SecurityConfig to require authentication for user endpoints but allow /login without auth

**Checkpoint**: Authentication flow fully functional and testable independently

---

## Phase 5: User Story 3 - Create and Retrieve Users (Priority: P1)

**Goal**: Enable authenticated API consumers to create new users and retrieve user details for onboarding and viewing user information

**Independent Test**: Create user with valid data, retrieve by ID, verify response matches expected format

### Tests for User Story 3 (MANDATORY)

- [ ] T061 [P] [US3] Create UserCrudIntegrationTest.java in src/test/java/com/example/specdriven/integration
- [ ] T062 [P] [US3] Write test: bootstrap mode - create first user without authentication returns 201
- [ ] T063 [P] [US3] Write test: create user with valid data returns 201 with User including server-assigned UUID
- [ ] T064 [P] [US3] Write test: retrieve user by ID returns 200 with complete user representation including roles
- [ ] T065 [P] [US3] Write test: password field not included in user response
- [ ] T066 [P] [US3] Write test: create/retrieve operations complete in under 1 second (95th percentile)
- [ ] T067 [P] [US3] Write test: create user with missing required fields returns 400 with VALIDATION_FAILED error
- [ ] T068 [P] [US3] Write test: create user with invalid field formats returns 400 with VALIDATION_FAILED error
- [ ] T069 [P] [US3] Write test: create user with duplicate email returns 409 with CONFLICT error
- [ ] T070 [P] [US3] Write test: retrieve non-existent user returns 404 with RESOURCE_NOT_FOUND error
- [ ] T071 [P] [US3] Write test: create user without authentication after bootstrap returns 401 with AUTHENTICATION_REQUIRED error
- [ ] T072 [P] [US3] Write test: database connection failure returns 503 with SERVICE_UNAVAILABLE error
- [ ] T073 [P] [US3] Write test: user endpoints gated by FeatureFlag.usersApi (returns 404 when disabled)

### Unit Tests for User Story 3

- [ ] T074 [P] [US3] Create UserServiceTest.java in src/test/java/com/example/specdriven/service
- [ ] T075 [P] [US3] Create UserMapperTest.java in src/test/java/com/example/specdriven/mapper
- [ ] T076 [P] [US3] Create UserRepositoryTest.java in src/test/java/com/example/specdriven/repository

### Implementation for User Story 3

- [ ] T077 [P] [US3] Create UserService.java in src/main/java/com/example/specdriven/service with create and retrieve methods including bootstrap mode logic
- [ ] T078 [US3] Create UsersController.java in src/main/java/com/example/specdriven/controller implementing generated UsersApi interface
- [ ] T079 [US3] Implement createUser() method delegating to UserService with validation and error handling
- [ ] T080 [US3] Implement getUserById() method delegating to UserService with error handling
- [ ] T081 [US3] Update UserMapper to hash passwords using BCryptPasswordEncoder during entity creation
- [ ] T082 [US3] Update UserMapper to never include password field when converting entity to DTO
- [ ] T083 [US3] Update SecurityConfig to implement bootstrap mode logic (allow POST /users without auth when user count = 0)

**Checkpoint**: User creation and retrieval fully functional and testable independently

---

## Phase 6: User Story 4 - Update and Delete Users (Priority: P1)

**Goal**: Enable authenticated API consumers to update user information and delete users for maintaining accurate records and lifecycle management

**Independent Test**: Create user, update fields, verify changes, delete user, confirm removal

### Tests for User Story 4 (MANDATORY)

- [ ] T084 [P] [US4] Create update/delete tests in UserCrudIntegrationTest.java
- [ ] T085 [P] [US4] Write test: update user with valid data returns 200 with updated user representation
- [ ] T086 [P] [US4] Write test: delete user returns 204 with no content
- [ ] T087 [P] [US4] Write test: retrieve deleted user returns 404 with RESOURCE_NOT_FOUND error
- [ ] T088 [P] [US4] Write test: update/delete operations complete in under 1 second (95th percentile)
- [ ] T089 [P] [US4] Write test: update user with invalid field values returns 400 with VALIDATION_FAILED error
- [ ] T090 [P] [US4] Write test: update non-existent user returns 404 with RESOURCE_NOT_FOUND error
- [ ] T091 [P] [US4] Write test: delete non-existent user returns 404 with RESOURCE_NOT_FOUND error
- [ ] T092 [P] [US4] Write test: update user with conflicting email returns 409 with CONFLICT error
- [ ] T093 [P] [US4] Write test: update/delete without authentication returns 401 with AUTHENTICATION_REQUIRED error

### Unit Tests for User Story 4

- [ ] T094 [P] [US4] Add update and delete unit tests in UserServiceTest.java

### Implementation for User Story 4

- [ ] T095 [P] [US4] Add updateUser() method to UserService.java with validation and email uniqueness check
- [ ] T096 [P] [US4] Add deleteUser() method to UserService.java with cascade handling
- [ ] T097 [US4] Implement updateUser() method in UsersController.java delegating to UserService
- [ ] T098 [US4] Implement deleteUser() method in UsersController.java delegating to UserService
- [ ] T099 [US4] Update UserMapper to handle partial updates (only update non-null fields)

**Checkpoint**: User update and delete fully functional and testable independently

---

## Phase 7: User Story 5 - List and Filter Users (Priority: P2)

**Goal**: Enable authenticated API consumers to retrieve paginated lists of users with filtering for browsing and searching efficiently

**Independent Test**: Create multiple users, retrieve pages with various filters, verify pagination and filtering behavior

### Tests for User Story 5 (MANDATORY)

- [ ] T100 [P] [US5] Create UserListIntegrationTest.java in src/test/java/com/example/specdriven/integration
- [ ] T101 [P] [US5] Write test: list users with pagination returns 200 with UserPage containing items and metadata
- [ ] T102 [P] [US5] Write test: page size enforced (max 100 items per page)
- [ ] T103 [P] [US5] Write test: filter by username returns only matching users
- [ ] T104 [P] [US5] Write test: filter by emailAddress returns only matching users
- [ ] T105 [P] [US5] Write test: filter by name returns partial case-insensitive matches
- [ ] T106 [P] [US5] Write test: filter by roleName returns only users with that role
- [ ] T107 [P] [US5] Write test: multiple filters applied with AND logic
- [ ] T108 [P] [US5] Write test: pagination metadata accurate (totalCount, totalPages calculated correctly)
- [ ] T109 [P] [US5] Write test: list operation completes in under 1 second (95th percentile)
- [ ] T110 [P] [US5] Write test: omit required pagination parameters returns 400 with VALIDATION_FAILED error
- [ ] T111 [P] [US5] Write test: invalid pagination parameters (negative page, zero/excessive pageSize) return 400 with VALIDATION_FAILED error
- [ ] T112 [P] [US5] Write test: unsupported query parameters return 400 with VALIDATION_FAILED error
- [ ] T113 [P] [US5] Write test: list without authentication returns 401 with AUTHENTICATION_REQUIRED error
- [ ] T114 [P] [US5] Write test: empty result set returns 200 with empty items array and valid pagination metadata

### Unit Tests for User Story 5

- [ ] T115 [P] [US5] Add list and filter unit tests in UserServiceTest.java

### Implementation for User Story 5

- [ ] T116 [P] [US5] Add custom query methods to UserRepository.java for pagination and filtering (LIMIT/OFFSET with WHERE clauses)
- [ ] T117 [P] [US5] Add listUsers() method to UserService.java with pagination and filter logic
- [ ] T118 [US5] Implement listUsers() method in UsersController.java delegating to UserService
- [ ] T119 [US5] Implement pagination metadata calculation (totalPages = ceil(totalCount / pageSize))
- [ ] T120 [US5] Ensure UserMapper loads roles when converting entities to DTOs for list results

**Checkpoint**: User list and filtering fully functional and testable independently

---

## Phase 8: User Story 6 - Manage User Roles (Priority: P2)

**Goal**: Enable authenticated API consumers to assign and remove roles from users for controlling permissions and access levels

**Independent Test**: Assign roles to user, verify in user representation, remove roles, confirm removal

### Tests for User Story 6 (MANDATORY)

- [ ] T121 [P] [US6] Create RoleManagementIntegrationTest.java in src/test/java/com/example/specdriven/integration
- [ ] T122 [P] [US6] Write test: assign role to user returns 204 and role appears in user's role list
- [ ] T123 [P] [US6] Write test: remove role from user returns 204 and role removed from user's role list
- [ ] T124 [P] [US6] Write test: assign already-assigned role is idempotent (returns 204, no duplicate)
- [ ] T125 [P] [US6] Write test: remove unassigned role is idempotent (returns 204, no error)
- [ ] T126 [P] [US6] Write test: assign role to non-existent user returns 404 with RESOURCE_NOT_FOUND error
- [ ] T127 [P] [US6] Write test: assign invalid role name returns 400 with VALIDATION_FAILED error
- [ ] T128 [P] [US6] Write test: assign/remove roles without authentication returns 401 with AUTHENTICATION_REQUIRED error

### Unit Tests for User Story 6

- [ ] T129 [P] [US6] Create RoleServiceTest.java in src/test/java/com/example/specdriven/service

### Implementation for User Story 6

- [ ] T130 [P] [US6] Create RoleService.java in src/main/java/com/example/specdriven/service with assignRole() and removeRole() methods (idempotent)
- [ ] T131 [US6] Create UserRolesController.java in src/main/java/com/example/specdriven/controller implementing generated UsersApi role endpoints
- [ ] T132 [US6] Implement assignRoleToUser() method delegating to RoleService
- [ ] T133 [US6] Implement removeRoleFromUser() method delegating to RoleService
- [ ] T134 [US6] Update UserService to load roles when retrieving users

**Checkpoint**: Role management fully functional and testable independently

---

## Phase 9: User Story 7 - Consistent Error Handling (Priority: P1)

**Goal**: Ensure all error responses follow consistent format with stable error codes for reliable client error handling

**Independent Test**: Trigger various error conditions across endpoints and verify consistent error response structure

### Tests for User Story 7 (MANDATORY)

- [ ] T135 [P] [US7] Create ErrorHandlingIntegrationTest.java in src/test/java/com/example/specdriven/integration
- [ ] T136 [P] [US7] Write test: all error responses contain code and message fields
- [ ] T137 [P] [US7] Write test: validation failures return 400 with VALIDATION_FAILED code
- [ ] T138 [P] [US7] Write test: resource not found returns 404 with RESOURCE_NOT_FOUND code
- [ ] T139 [P] [US7] Write test: conflicts return 409 with CONFLICT code
- [ ] T140 [P] [US7] Write test: authentication failures return 401 with AUTHENTICATION_FAILED code
- [ ] T141 [P] [US7] Write test: internal errors return 500 with INTERNAL_ERROR code and safe message
- [ ] T142 [P] [US7] Write test: service unavailable returns 503 with SERVICE_UNAVAILABLE code

### Implementation for User Story 7

- [ ] T143 [US7] Verify GlobalExceptionHandler maps all exception types to appropriate HTTP status codes and error codes
- [ ] T144 [US7] Verify ErrorResponseFactory generates stable error codes for all error scenarios
- [ ] T145 [US7] Verify error messages are safe for display (no stack traces, secrets, or internal details)

**Checkpoint**: Error handling consistency verified across all endpoints

---

## Phase 10: User Story 8 - Retry Behavior via HTTP Semantics (Priority: P1)

**Goal**: Communicate retry behavior using only standard HTTP status codes and headers for simple standards-based retry logic

**Independent Test**: Trigger transient and permanent failures and verify status codes indicate correct retry behavior

### Tests for User Story 8 (MANDATORY)

- [ ] T146 [P] [US8] Create RetryBehaviorIntegrationTest.java in src/test/java/com/example/specdriven/integration
- [ ] T147 [P] [US8] Write test: temporary database unavailability returns 503 indicating retry may succeed
- [ ] T148 [P] [US8] Write test: 503 response may include Retry-After header with retry interval
- [ ] T149 [P] [US8] Write test: validation errors return 400 indicating no retry without changing request
- [ ] T150 [P] [US8] Write test: resource not found returns 404 indicating no retry will succeed
- [ ] T151 [P] [US8] Write test: no error response contains custom retryable field
- [ ] T152 [P] [US8] Write test: retry behavior determinable solely from HTTP status code family (4xx vs 5xx)

### Implementation for User Story 8

- [ ] T153 [US8] Verify GlobalExceptionHandler uses 4xx for permanent errors (validation, not found, conflict, auth)
- [ ] T154 [US8] Verify GlobalExceptionHandler uses 5xx for transient errors (internal error, service unavailable)
- [ ] T155 [US8] Verify ErrorResponse schema does not include retryable field
- [ ] T156 [US8] Add Retry-After header to 503 responses where appropriate

**Checkpoint**: Retry semantics verified using standard HTTP status codes only

---

## Phase 11: User Story 9 - Feature Flag Control (Priority: P1)

**Goal**: Enable system administrators to control feature availability via configuration properties for safe incremental rollout

**Independent Test**: Toggle FeatureFlag.usersApi and verify endpoint availability changes accordingly

### Tests for User Story 9 (MANDATORY)

- [ ] T157 [P] [US9] Create FeatureFlagIntegrationTest.java in src/test/java/com/example/specdriven/integration
- [ ] T158 [P] [US9] Write test: when usersApi=false, user management endpoints return 404 with standard error
- [ ] T159 [P] [US9] Write test: when usersApi=true, user management endpoints process requests normally
- [ ] T160 [P] [US9] Write test: health check endpoint always available regardless of feature flags
- [ ] T161 [P] [US9] Write test: feature disabled error does not reveal feature exists

### Unit Tests for User Story 9

- [ ] T162 [P] [US9] Create FeatureFlagSecurityFilterTest.java in src/test/java/com/example/specdriven/security

### Implementation for User Story 9

- [ ] T163 [US9] Verify FeatureFlagSecurityFilter checks FeatureFlag.usersApi before allowing requests to gated endpoints
- [ ] T164 [US9] Verify FeatureFlagSecurityFilter returns 404 with standard error body when flag disabled (no feature disclosure)
- [ ] T165 [US9] Verify SecurityConfig explicitly bypasses feature flag check for /ping endpoint
- [ ] T166 [US9] Document default feature flag value (false) in application.yml

**Checkpoint**: Feature flag control fully functional and testable

---

## Phase 12: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T167 [P] Update README.md with API usage examples and getting started guide
- [ ] T168 [P] Verify quickstart.md workflows work as documented
- [ ] T169 [P] Run performance smoke tests to validate ≤1s budget for 95% of operations
- [ ] T170 [P] Verify all error responses use stable codes and consistent structure
- [ ] T171 [P] Verify bootstrap mode logic works correctly (zero users vs one or more users)
- [ ] T172 [P] Verify pagination tested with multiple pages and empty results
- [ ] T173 [P] Verify role operations tested for idempotency
- [ ] T174 [P] Verify passwords never returned in responses
- [ ] T175 [P] Verify login errors don't enumerate usernames
- [ ] T176 [P] Verify no breaking changes to API contract
- [ ] T177 [P] Run full test suite with ./gradlew clean build and verify all tests pass
- [ ] T178 [P] Generate test coverage report and verify coverage meets requirements
- [ ] T179 Code cleanup and refactoring for consistency
- [ ] T180 Security review: verify JWT secret externalized, BCrypt used for passwords, no sensitive data in logs

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-11)**: All depend on Foundational phase completion
  - US1, US2, US3, US4 are P1 and should be completed first
  - US5, US6 are P2 and can be done after P1 stories
  - US7, US8, US9 are P1 cross-cutting concerns that validate all endpoints
  - Stories can proceed in parallel if staffed
- **Polish (Phase 12)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1) - Health Check**: Can start after Foundational - No dependencies on other stories
- **User Story 2 (P1) - Authentication**: Can start after Foundational - No dependencies on other stories
- **User Story 3 (P1) - Create/Retrieve Users**: Depends on US2 (authentication) - Should follow sequentially
- **User Story 4 (P1) - Update/Delete Users**: Depends on US3 (users exist) - Should follow sequentially
- **User Story 5 (P2) - List/Filter Users**: Depends on US3 (users exist) - Can start after US3
- **User Story 6 (P2) - Manage Roles**: Depends on US3 (users exist) - Can start after US3
- **User Story 7 (P1) - Error Handling**: Validates all endpoints - Should run after US1-US6 implemented
- **User Story 8 (P1) - Retry Behavior**: Validates all endpoints - Should run after US1-US6 implemented
- **User Story 9 (P1) - Feature Flags**: Affects all endpoints - Should run after US1-US6 implemented

### Within Each User Story

- Tests can be written in parallel before implementation
- Models before services
- Services before controllers
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- All tests for a user story marked [P] can run in parallel
- Models/entities marked [P] within a story can run in parallel
- After dependencies met, multiple stories can be worked on by different team members:
  - US1 (Health Check) - Independent, can start after Phase 2
  - US2 (Authentication) - Independent, can start after Phase 2
  - Once US2 complete, US3 can start
  - Once US3 complete, US4, US5, US6 can run in parallel
  - US7, US8, US9 validate all endpoints after implementation

---

## Parallel Example: Foundational Phase

```bash
# Launch all entity creation tasks together:
Task T014: Create UserEntity.java
Task T015: Create RoleEntity.java
Task T016: Create PermissionEntity.java
Task T017: Create UserRoleEntity.java
Task T018: Create RolePermissionEntity.java

# Launch all repository tasks together:
Task T019: Create UserRepository.java
Task T020: Create UserRoleRepository.java

# Launch all exception classes together:
Task T027-T030: Create exception classes
```

## Parallel Example: User Story 3 Tests

```bash
# Launch all US3 tests together:
Task T061: Create UserCrudIntegrationTest.java
Task T062-T073: Write all integration tests for US3
Task T074: Create UserServiceTest.java
Task T075: Create UserMapperTest.java
Task T076: Create UserRepositoryTest.java
```

---

## Implementation Strategy

### MVP First (User Stories 1, 2, 3, 4 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Health Check)
4. Complete Phase 4: User Story 2 (Authentication)
5. Complete Phase 5: User Story 3 (Create/Retrieve Users)
6. Complete Phase 6: User Story 4 (Update/Delete Users)
7. **STOP and VALIDATE**: Test MVP independently
8. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add US1 → Test independently → Deploy/Demo (Health check working)
3. Add US2 → Test independently → Deploy/Demo (Auth working)
4. Add US3 → Test independently → Deploy/Demo (User CRUD partial)
5. Add US4 → Test independently → Deploy/Demo (User CRUD complete - MVP!)
6. Add US5 → Test independently → Deploy/Demo (List/filter added)
7. Add US6 → Test independently → Deploy/Demo (Role management added)
8. Validate US7, US8, US9 → Deploy/Demo (Full validation complete)
9. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 → User Story 2 → User Story 3
   - Developer B: User Story 4 (after US3) → User Story 5 (after US3)
   - Developer C: User Story 6 (after US3) → User Story 9
3. After US1-US6 complete:
   - Developer A: User Story 7
   - Developer B: User Story 8
   - Developer C: Polish phase
4. Stories complete and integrate independently

---

## Task Summary

### Total Tasks: 180

### Tasks by Phase:
- **Phase 1 (Setup)**: 10 tasks
- **Phase 2 (Foundational)**: 25 tasks
- **Phase 3 (US1 - Health Check)**: 8 tasks
- **Phase 4 (US2 - Authentication)**: 17 tasks
- **Phase 5 (US3 - Create/Retrieve)**: 23 tasks
- **Phase 6 (US4 - Update/Delete)**: 16 tasks
- **Phase 7 (US5 - List/Filter)**: 21 tasks
- **Phase 8 (US6 - Manage Roles)**: 14 tasks
- **Phase 9 (US7 - Error Handling)**: 11 tasks
- **Phase 10 (US8 - Retry Behavior)**: 11 tasks
- **Phase 11 (US9 - Feature Flags)**: 10 tasks
- **Phase 12 (Polish)**: 14 tasks

### Tasks by User Story:
- **US1 (Health Check)**: 8 tasks
- **US2 (Authentication)**: 17 tasks
- **US3 (Create/Retrieve Users)**: 23 tasks
- **US4 (Update/Delete Users)**: 16 tasks
- **US5 (List/Filter Users)**: 21 tasks
- **US6 (Manage Roles)**: 14 tasks
- **US7 (Error Handling)**: 11 tasks
- **US8 (Retry Behavior)**: 11 tasks
- **US9 (Feature Flags)**: 10 tasks

### Parallel Opportunities Identified:
- **Setup phase**: 6 parallel tasks (T003-T010 excluding T001, T002, T007)
- **Foundational phase**: 20+ parallel tasks across entities, repositories, security, exceptions, mappers
- **Each user story**: Tests can be written in parallel, multiple team members can work on different stories after dependencies met

### Independent Test Criteria:
- **US1**: Call /ping without auth, verify 200 with "pong"
- **US2**: Login with credentials, use token to access protected endpoint
- **US3**: Create user, retrieve by ID, verify format
- **US4**: Create, update, delete user, verify lifecycle
- **US5**: Create multiple users, retrieve with filters, verify pagination
- **US6**: Assign/remove roles, verify in user representation
- **US7**: Trigger errors across endpoints, verify consistent format
- **US8**: Trigger transient/permanent failures, verify status codes
- **US9**: Toggle feature flag, verify endpoint availability

### Suggested MVP Scope:
- **Minimum MVP**: US1 (Health Check) + US2 (Authentication) + US3 (Create/Retrieve)
- **Recommended MVP**: US1 + US2 + US3 + US4 (Complete user lifecycle)
- **Full Feature**: All user stories (US1-US9)

---

## Notes

- [P] tasks = different files, no dependencies, can run in parallel
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Tests written before implementation where practical (TDD approach)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- All tasks include exact file paths for clarity
- Format validation: All tasks follow checklist format (checkbox, ID, labels, file paths)

---

**Tasks Status**: ✅ Complete - Ready for Implementation
**Last Updated**: 2025-12-29
**Next Step**: Begin Phase 1 (Setup) with `./gradlew clean build`
