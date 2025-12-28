# Tasks: SpecDriven API System

**Feature**: Overall System Architecture  
**Branch**: `main`  
**Date**: 2025-12-27  
**Input**: Design documents from `specs/main/`

**Tech Stack**: Java 17, Spring Boot 3.5.9, Gradle, Spring Data JDBC, Spring Security, JWT, Flyway, H2 Database, OpenAPI Generator 7.14.0

**Prerequisites**: Phase 0 (research) and Phase 1 (design) are complete. All design artifacts available:
- ✅ `specs/main/spec.md` - User stories with priorities
- ✅ `specs/main/plan.md` - Technical context and architecture
- ✅ `specs/main/research.md` - Technology decisions
- ✅ `specs/main/data-model.md` - Entity definitions
- ✅ `specs/main/contracts/openapi.contract.yaml` - OpenAPI specification
- ✅ `specs/main/quickstart.md` - Developer guide

**Implementation Notes**:
- Use Lombok annotations (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor) for all handwritten entities and services
- OpenAPI Generator creates DTOs and interfaces in `src/main-gen/java` (never manually edit)
- Handwritten implementations go in `src/main/java`
- All endpoints gated by feature flag `FeatureFlag.usersApi`
- Tests are MANDATORY for all user stories (happy path + negative scenarios)

---

## Format: `- [ ] [TaskID] [P?] [Story?] Description with file path`

- **Checkbox**: Always start with `- [ ]`
- **[TaskID]**: Sequential (T001, T002, etc.)
- **[P]**: Parallelizable (different files, no dependencies)
- **[Story]**: User story label (US1, US2, etc.) - required for user story phases only
- **File paths**: Include exact paths in descriptions

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Basic project structure and build configuration

- [ ] T001 Verify Gradle wrapper and Java 17 toolchain configuration in build.gradle
- [ ] T002 Configure OpenAPI Generator plugin in build.gradle with Spring generator (interfaceOnly=true, useSpringBoot3=true, useJakartaEe=true)
- [ ] T003 [P] Set up source directories: src/main/java, src/main/resources, src/main-gen/java, src/test/java
- [ ] T004 [P] Copy OpenAPI specification from specs/main/contracts/openapi.contract.yaml to src/main/resources/openapi.yaml
- [ ] T005 [P] Configure Lombok in build.gradle (compileOnly and annotationProcessor dependencies)
- [ ] T006 Run OpenAPI code generation to verify setup: ./gradlew openApiGenerate

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Configuration & Infrastructure

- [ ] T007 Create SpecDrivenApplication.java main class with @SpringBootApplication in src/main/java/com/example/specdriven/
- [ ] T008 [P] Configure application.properties: H2 database (PostgreSQL mode), Flyway settings, JWT config (secret, expiration)
- [ ] T009 [P] Implement FeatureFlagProperties class in src/main/java/com/example/specdriven/config/ with @ConfigurationProperties for feature-flag namespace
- [ ] T010 [P] Add feature-flag.users-api=false to application.properties as default
- [ ] T011 Create SecurityConfig class in src/main/java/com/example/specdriven/config/ with @Configuration and @EnableWebSecurity

### Error Handling Infrastructure

- [ ] T012 [P] Create ErrorCode enum in src/main/java/com/example/specdriven/error/ with all error codes (VALIDATION_FAILED, UNAUTHORIZED, FORBIDDEN, RESOURCE_NOT_FOUND, FEATURE_DISABLED, CONFLICT, INTERNAL_ERROR, SERVICE_UNAVAILABLE)
- [ ] T013 [P] Create custom exception classes in src/main/java/com/example/specdriven/error/: ValidationException, UnauthorizedException, ForbiddenException, ResourceNotFoundException, FeatureDisabledException, ConflictException
- [ ] T014 Create GlobalExceptionHandler with @ControllerAdvice in src/main/java/com/example/specdriven/error/ to map exceptions to ErrorResponse with stable codes and HTTP status
- [ ] T015 [P] Create unit tests for GlobalExceptionHandler in src/test/java/com/example/specdriven/error/GlobalExceptionHandlerTest.java

### Database Schema

- [ ] T016 Create Flyway migration V001__create_users_table.sql in src/main/resources/db/migration/ with users table (id UUID, username, name, password, email_address, created_at, updated_at) and unique indexes
- [ ] T017 [P] Create Flyway migration V002__create_user_roles_table.sql in src/main/resources/db/migration/ with user_roles table (user_id, role_name composite PK, FK to users, CHECK constraint for valid roles)

### Domain Models & Enums

- [ ] T018 [P] Create RoleName enum in src/main/java/com/example/specdriven/users/model/ with values (ADMIN, USER, AUDITOR) matching OpenAPI schema
- [ ] T019 [P] Create Permission enum in src/main/java/com/example/specdriven/users/model/ with values (USER_READ, USER_WRITE, ROLE_ASSIGN) matching OpenAPI schema
- [ ] T020 [P] Create Role class in src/main/java/com/example/specdriven/users/model/ with Lombok annotations (@Data, @Builder) containing roleName and permissions list
- [ ] T021 Create User entity class in src/main/java/com/example/specdriven/users/model/ with Lombok annotations, @Table("users"), Spring Data JDBC annotations, and @JsonProperty(access=WRITE_ONLY) for password

### Security & JWT

- [ ] T022 Create JwtService class in src/main/java/com/example/specdriven/security/ for token generation and validation (using io.jsonwebtoken library)
- [ ] T023 Create JwtAuthenticationFilter class in src/main/java/com/example/specdriven/security/ extending OncePerRequestFilter to validate bearer tokens
- [ ] T024 Create UserDetailsServiceImpl in src/main/java/com/example/specdriven/security/ implementing UserDetailsService to load users for authentication
- [ ] T025 Update SecurityConfig to configure JWT filter chain, permit /ping and /login without auth, protect all other endpoints
- [ ] T026 [P] Create unit tests for JwtService in src/test/java/com/example/specdriven/security/JwtServiceTest.java

### Repository Layer

- [ ] T027 [P] Create UserRepository interface in src/main/java/com/example/specdriven/users/repository/ extending CrudRepository<User, UUID> with custom query methods (findByUsername, existsByUsername, existsByEmailAddress)
- [ ] T028 [P] Create UserRoleRepository interface in src/main/java/com/example/specdriven/users/repository/ with methods to manage user-role relationships

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - System Health Check (Priority: P1) 🎯 MVP

**Goal**: Implement lightweight health check endpoint requiring no authentication for monitoring system availability

**Independent Test**: Call GET /ping and verify 200 response with pong message. Test with and without authentication header.

### Implementation for User Story 1

- [ ] T029 [P] [US1] Create PingController class in src/main/java/com/example/specdriven/ping/ implementing generated PingApi interface with ping() method returning PingResponse
- [ ] T030 [P] [US1] Create integration test PingControllerTest in src/test/java/com/example/specdriven/ping/PingControllerTest.java testing happy path (200 with pong message)
- [ ] T031 [P] [US1] Add negative test to PingControllerTest for simulated internal error scenario returning 500 with ErrorResponse

**Checkpoint**: User Story 1 complete - health check endpoint fully functional and independently testable

---

## Phase 4: User Story 2 - Centralized Error Handling (Priority: P1)

**Goal**: Ensure all API errors follow consistent format with stable error codes for reliable client error handling

**Independent Test**: Trigger various error conditions (validation failure, not found, conflict, unauthorized, forbidden, 500) and verify consistent ErrorResponse structure with appropriate codes and HTTP status

### Implementation for User Story 2

- [ ] T032 [P] [US2] Add integration test ErrorResponseContractTest in src/test/java/com/example/specdriven/error/ErrorResponseContractTest.java to verify error response structure
- [ ] T033 [P] [US2] Test validation failure (400) returns VALIDATION_FAILED code in ErrorResponseContractTest
- [ ] T034 [P] [US2] Test not found (404) returns RESOURCE_NOT_FOUND code in ErrorResponseContractTest
- [ ] T035 [P] [US2] Test unauthorized (401) returns UNAUTHORIZED code in ErrorResponseContractTest
- [ ] T036 [P] [US2] Test forbidden (403) returns FORBIDDEN code in ErrorResponseContractTest
- [ ] T037 [P] [US2] Test conflict (409) returns CONFLICT code in ErrorResponseContractTest
- [ ] T038 [P] [US2] Test internal error (500) returns INTERNAL_ERROR code and doesn't expose sensitive details in ErrorResponseContractTest
- [ ] T039 [US2] Review and validate GlobalExceptionHandler (T014) handles all exception types with correct HTTP status and error codes
- [ ] T040 [US2] Add logging to GlobalExceptionHandler for all errors with appropriate log levels (WARN for 4xx, ERROR for 5xx)

**Checkpoint**: User Story 2 complete - error handling is consistent across all endpoints

---

## Phase 5: User Story 3 - Feature Flag Management (Priority: P1)

**Goal**: Enable/disable features via configuration properties for controlled feature rollout without code changes

**Independent Test**: Toggle feature-flag.users-api between true/false and verify endpoint behavior changes (404 when disabled, normal processing when enabled)

### Implementation for User Story 3

- [ ] T041 [P] [US3] Create FeatureFlagInterceptor in src/main/java/com/example/specdriven/feature/ implementing HandlerInterceptor to check feature flags and return 404 with FEATURE_DISABLED when disabled
- [ ] T042 [US3] Configure FeatureFlagInterceptor in WebMvcConfigurer to intercept /users, /login endpoints checking FeatureFlag.usersApi property
- [ ] T043 [P] [US3] Create integration test FeatureFlagTest in src/test/java/com/example/specdriven/feature/FeatureFlagTest.java testing users API with flag disabled (expect 404)
- [ ] T044 [P] [US3] Add test to FeatureFlagTest with flag enabled (expect normal processing)
- [ ] T045 [P] [US3] Add test to FeatureFlagTest verifying error response includes FEATURE_DISABLED code when flag is off

**Checkpoint**: User Story 3 complete - feature flags control endpoint availability

---

## Phase 6: User Story 4 - Authentication & Authorization (Priority: P1)

**Goal**: Implement JWT-based authentication allowing API consumers to authenticate and access protected resources

**Independent Test**: Login with valid credentials, receive JWT token, use token to access protected endpoint. Verify bootstrap mode allows first user creation without auth.

### Service Layer for User Story 4

- [ ] T046 [US4] Create UserService class in src/main/java/com/example/specdriven/users/service/ with Lombok @Service annotation
- [ ] T047 [P] [US4] Implement createUser() method in UserService using BCryptPasswordEncoder to hash passwords, checking for bootstrap mode (zero users)
- [ ] T048 [P] [US4] Implement getUserById() method in UserService throwing ResourceNotFoundException if not found
- [ ] T049 [P] [US4] Implement getUserByUsername() method in UserService for authentication
- [ ] T050 [P] [US4] Implement countUsers() method in UserService for bootstrap mode detection

### Authentication Endpoints for User Story 4

- [ ] T051 [US4] Create LoginController in src/main/java/com/example/specdriven/users/controller/ implementing generated LoginApi interface
- [ ] T052 [US4] Implement login() method in LoginController validating credentials, generating JWT token on success
- [ ] T053 [US4] Update SecurityConfig to configure bootstrap mode: allow POST /users without auth when zero users exist
- [ ] T054 [US4] Implement bootstrap check logic in security configuration using custom authentication provider

### Tests for User Story 4

- [ ] T055 [P] [US4] Create integration test AuthenticationTest in src/test/java/com/example/specdriven/users/AuthenticationTest.java testing valid login returns 200 with JWT token
- [ ] T056 [P] [US4] Add test to AuthenticationTest for invalid credentials returning 400 with generic error (not revealing username existence)
- [ ] T057 [P] [US4] Add test to AuthenticationTest for protected endpoint without token returning 401 UNAUTHORIZED
- [ ] T058 [P] [US4] Add test to AuthenticationTest for protected endpoint with invalid token returning 401 UNAUTHORIZED
- [ ] T059 [P] [US4] Add test to AuthenticationTest for protected endpoint with valid token returning successful response
- [ ] T060 [P] [US4] Create bootstrap mode test BootstrapModeTest in src/test/java/com/example/specdriven/users/BootstrapModeTest.java verifying POST /users succeeds without auth when zero users
- [ ] T061 [P] [US4] Add test to BootstrapModeTest verifying POST /users requires auth after first user created

**Checkpoint**: User Story 4 complete - authentication and authorization fully functional

---

## Phase 7: User Story 5 - User Management CRUD (Priority: P2)

**Goal**: Enable authenticated users to create, read, update, and delete users for directory management

**Independent Test**: Perform full CRUD lifecycle - create user (201), retrieve user (200), update user (200), delete user (204), verify deletion (404)

### Controller Implementation for User Story 5

- [ ] T062 [US5] Create UsersController in src/main/java/com/example/specdriven/users/controller/ implementing generated UsersApi interface
- [ ] T063 [P] [US5] Implement createUser() method in UsersController delegating to UserService, returning 201 with created user
- [ ] T064 [P] [US5] Implement getUserById() method in UsersController delegating to UserService
- [ ] T065 [P] [US5] Implement updateUser() method in UsersController validating at least one field provided, checking for conflicts (duplicate username/email)
- [ ] T066 [P] [US5] Implement deleteUser() method in UsersController returning 204 on success

### Service Methods for User Story 5

- [ ] T067 [P] [US5] Implement updateUser() method in UserService handling partial updates, validating uniqueness constraints, hashing password if provided
- [ ] T068 [P] [US5] Implement deleteUser() method in UserService throwing ResourceNotFoundException if user doesn't exist
- [ ] T069 [US5] Add validation in UserService.createUser() to throw ConflictException for duplicate username or email

### Tests for User Story 5

- [ ] T070 [P] [US5] Create integration test UserCrudTest in src/test/java/com/example/specdriven/users/UserCrudTest.java testing create user with valid data returns 201
- [ ] T071 [P] [US5] Add test to UserCrudTest for get user by ID returns 200 with user details
- [ ] T072 [P] [US5] Add test to UserCrudTest for update user returns 200 with updated details
- [ ] T073 [P] [US5] Add test to UserCrudTest for delete user returns 204
- [ ] T074 [P] [US5] Add test to UserCrudTest for get deleted user returns 404
- [ ] T075 [P] [US5] Add test to UserCrudTest verifying password never appears in response JSON
- [ ] T076 [P] [US5] Add negative test to UserCrudTest for invalid user data returns 400 VALIDATION_FAILED
- [ ] T077 [P] [US5] Add negative test to UserCrudTest for non-existent user ID returns 404 RESOURCE_NOT_FOUND
- [ ] T078 [P] [US5] Add negative test to UserCrudTest for duplicate username returns 409 CONFLICT
- [ ] T079 [P] [US5] Add negative test to UserCrudTest for duplicate email returns 409 CONFLICT
- [ ] T080 [P] [US5] Add negative test to UserCrudTest for update with no fields provided returns 400 VALIDATION_FAILED

**Checkpoint**: User Story 5 complete - full CRUD operations on users functional and tested

---

## Phase 8: User Story 6 - Paginated User Listing (Priority: P2)

**Goal**: Enable efficient browsing of users with pagination to prevent loading unbounded result sets

**Independent Test**: Create multiple users, retrieve with different page/pageSize parameters, verify pagination metadata (page, pageSize, totalPages, totalElements)

### Service & Controller Implementation for User Story 6

- [ ] T081 [US6] Implement listUsers() method in UserService accepting page, pageSize, and optional filters (username, emailAddress, name, roleName)
- [ ] T082 [US6] Add pagination logic in UserService.listUsers() using Spring Data Pageable, calculating totalPages and totalElements
- [ ] T083 [US6] Implement filter logic in UserService.listUsers() for exact match on username/emailAddress, partial match on name, role filter
- [ ] T084 [US6] Implement listUsers() method in UsersController delegating to UserService, returning UserPage response

### Repository Enhancements for User Story 6

- [ ] T085 [P] [US6] Add findAll with Pageable method to UserRepository
- [ ] T086 [P] [US6] Add custom query methods to UserRepository for filtered searches (by username, email, name LIKE, role)

### Tests for User Story 6

- [ ] T087 [P] [US6] Create integration test UserListingTest in src/test/java/com/example/specdriven/users/UserListingTest.java testing list users with pagination returns 200 with UserPage
- [ ] T088 [P] [US6] Add test to UserListingTest verifying pagination metadata is correct (page, pageSize, totalPages, totalElements)
- [ ] T089 [P] [US6] Add test to UserListingTest for filter by username (exact match)
- [ ] T090 [P] [US6] Add test to UserListingTest for filter by emailAddress (exact match)
- [ ] T091 [P] [US6] Add test to UserListingTest for filter by name (partial, case-insensitive match)
- [ ] T092 [P] [US6] Add test to UserListingTest for filter by roleName
- [ ] T093 [P] [US6] Add test to UserListingTest for empty results (no matching users)
- [ ] T094 [P] [US6] Add test to UserListingTest for last page (fewer items than pageSize)
- [ ] T095 [P] [US6] Add test to UserListingTest for max page size boundary (100 items)
- [ ] T096 [P] [US6] Add negative test to UserListingTest for missing pagination parameters returns 400 VALIDATION_FAILED
- [ ] T097 [P] [US6] Add negative test to UserListingTest for invalid pagination parameters (page < 1, pageSize > 100) returns 400 VALIDATION_FAILED

**Checkpoint**: User Story 6 complete - paginated user listing with filters functional and tested

---

## Phase 9: User Story 7 - Role Assignment (Priority: P2)

**Goal**: Enable role management by allowing assignment and removal of roles from users for permission control

**Independent Test**: Assign role to user (204), verify role in user details, remove role (204), verify role removed. Test idempotency of both operations.

### Service Implementation for User Story 7

- [ ] T098 [US7] Implement assignRoleToUser() method in UserService (idempotent - ignore if already exists)
- [ ] T099 [US7] Implement removeRoleFromUser() method in UserService (idempotent - ignore if doesn't exist)
- [ ] T100 [US7] Add validation in UserService role methods to throw ResourceNotFoundException if user doesn't exist
- [ ] T101 [US7] Add validation in UserService role methods to throw ValidationException if roleName is invalid (not in RoleName enum)
- [ ] T102 [US7] Update getUserById() in UserService to join and populate user roles from user_roles table

### Controller Implementation for User Story 7

- [ ] T103 [P] [US7] Implement assignRoleToUser() method in UsersController returning 204 on success
- [ ] T104 [P] [US7] Implement removeRoleFromUser() method in UsersController returning 204 on success

### Tests for User Story 7

- [ ] T105 [P] [US7] Create integration test RoleAssignmentTest in src/test/java/com/example/specdriven/users/RoleAssignmentTest.java testing assign role returns 204
- [ ] T106 [P] [US7] Add test to RoleAssignmentTest verifying user has role after assignment (via getUserById)
- [ ] T107 [P] [US7] Add test to RoleAssignmentTest for remove role returns 204
- [ ] T108 [P] [US7] Add test to RoleAssignmentTest verifying user doesn't have role after removal
- [ ] T109 [P] [US7] Add test to RoleAssignmentTest for assign role idempotency (assign twice, still succeeds)
- [ ] T110 [P] [US7] Add test to RoleAssignmentTest for remove role idempotency (remove twice, still succeeds)
- [ ] T111 [P] [US7] Add test to RoleAssignmentTest for multiple roles on same user
- [ ] T112 [P] [US7] Add negative test to RoleAssignmentTest for non-existent user returns 404 RESOURCE_NOT_FOUND
- [ ] T113 [P] [US7] Add negative test to RoleAssignmentTest for invalid roleName returns 400 VALIDATION_FAILED

**Checkpoint**: User Story 7 complete - role assignment and removal fully functional and tested

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements, documentation, and validation

### Documentation & Validation

- [ ] T114 [P] Update README.md with project overview, build instructions, and quickstart reference
- [ ] T115 [P] Validate quickstart.md scenarios work end-to-end (bootstrap user creation, login, CRUD operations, role assignment)
- [ ] T116 [P] Add JavaDoc comments to all public service methods explaining purpose, parameters, exceptions
- [ ] T117 [P] Add JavaDoc comments to all controller methods with OpenAPI operation references

### Code Quality & Testing

- [ ] T118 [P] Review and enhance logging across all services (INFO for successful operations, WARN for business errors, ERROR for technical errors)
- [ ] T119 [P] Add unit tests for UserService methods in src/test/java/com/example/specdriven/users/UserServiceTest.java
- [ ] T120 [P] Verify all tests pass: ./gradlew clean test
- [ ] T121 [P] Run OpenAPI validation to ensure generated code matches specification
- [ ] T122 Perform end-to-end smoke test with feature flag enabled following quickstart.md scenarios

### Security Hardening

- [ ] T123 [P] Review SecurityConfig for any security misconfigurations
- [ ] T124 [P] Verify password fields are excluded from JSON serialization in all response paths
- [ ] T125 [P] Verify error messages don't leak sensitive information (e.g., username existence in login failures)
- [ ] T126 Add security test verifying JWT tokens expire correctly

### Performance & Production Readiness

- [ ] T127 [P] Verify database indexes exist for username and email_address columns
- [ ] T128 [P] Test pagination performance with larger datasets (100+ users)
- [ ] T129 [P] Verify all endpoints respond within 1 second performance budget
- [ ] T130 Create application-prod.properties template with production configuration notes (change JWT secret, use PostgreSQL, etc.)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies - start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 completion - **BLOCKS ALL USER STORIES**
- **Phase 3 (US1 - Health Check)**: Depends on Phase 2 completion
- **Phase 4 (US2 - Error Handling)**: Depends on Phase 2 completion (can run parallel with US1)
- **Phase 5 (US3 - Feature Flags)**: Depends on Phase 2 completion (can run parallel with US1, US2)
- **Phase 6 (US4 - Authentication)**: Depends on Phase 2 completion, logically after US1-US3 for cleaner testing
- **Phase 7 (US5 - User CRUD)**: Depends on Phase 6 (US4) for authentication infrastructure
- **Phase 8 (US6 - User Listing)**: Depends on Phase 7 (US5) for user CRUD, but can overlap implementation
- **Phase 9 (US7 - Role Assignment)**: Depends on Phase 7 (US5) for user management, can overlap with US6
- **Phase 10 (Polish)**: Depends on all desired user stories being complete

### Critical Path

```
Phase 1 (Setup) 
  → Phase 2 (Foundational) **CRITICAL BLOCKER**
    → Phase 3 (US1) + Phase 4 (US2) + Phase 5 (US3) [parallel]
      → Phase 6 (US4)
        → Phase 7 (US5)
          → Phase 8 (US6) + Phase 9 (US7) [parallel]
            → Phase 10 (Polish)
```

### User Story Independence

- **US1 (Health Check)**: Fully independent, no dependencies on other stories
- **US2 (Error Handling)**: Independent, enhances all stories
- **US3 (Feature Flags)**: Independent, gates all user/auth features
- **US4 (Authentication)**: Depends on error handling and feature flags working
- **US5 (User CRUD)**: Depends on authentication
- **US6 (User Listing)**: Depends on US5 for users to exist
- **US7 (Role Assignment)**: Depends on US5 for users to exist

### Within Each User Story

1. **Tests first** (where practical) - write failing tests before implementation
2. **Models** before services (entities, enums, DTOs)
3. **Services** before controllers (business logic isolation)
4. **Controllers** implement generated API interfaces (wire services to endpoints)
5. **Integration tests** verify full request/response flow

### Parallel Opportunities

**During Phase 2 (Foundational)**:
- T012-T015 (Error handling) || T018-T021 (Models) || T027-T028 (Repositories)
- T022-T026 (Security/JWT) can overlap with above

**After Phase 2 Complete**:
- Phase 3 (US1) || Phase 4 (US2) || Phase 5 (US3) - all can run in parallel

**Within User Stories**:
- All test tasks marked [P] can run in parallel
- Model creation tasks marked [P] can run in parallel
- Documentation tasks (T114-T117) can run in parallel

---

## Parallel Execution Example: User Story 5 (User CRUD)

If you have multiple developers, these can run simultaneously:

**Developer A**:
- T070-T080 (All integration tests for CRUD)

**Developer B**:
- T063 (createUser controller)
- T067 (updateUser service)

**Developer C**:
- T064 (getUserById controller)
- T068 (deleteUser service)

All converge when T062 (controller creation) and T066 (deleteUser controller) complete the implementation.

---

## Implementation Strategy

### MVP First (Minimal Viable Product)

**Recommended MVP Scope**: User Stories 1-4 only

1. ✅ Complete Phase 1 (Setup) - 6 tasks
2. ✅ Complete Phase 2 (Foundational) - 22 tasks **CRITICAL**
3. ✅ Complete Phase 3 (US1 - Health Check) - 3 tasks
4. ✅ Complete Phase 4 (US2 - Error Handling) - 9 tasks
5. ✅ Complete Phase 5 (US3 - Feature Flags) - 5 tasks
6. ✅ Complete Phase 6 (US4 - Authentication) - 16 tasks
7. **STOP and VALIDATE**: 
   - Health check works
   - Errors are consistent
   - Feature flags control access
   - Authentication works (login, JWT, bootstrap mode)
8. **Deploy/Demo MVP** (61 tasks total)

**MVP Deliverables**:
- Functioning health check endpoint
- Consistent error responses across all endpoints
- Feature flag controlling users API access
- JWT authentication with bootstrap mode
- Ready for user management features

### Incremental Delivery (Full Feature Set)

**After MVP validated**:

1. Add Phase 7 (US5 - User CRUD) - 19 tasks → Validate independently
2. Add Phase 8 (US6 - User Listing) - 17 tasks → Validate independently  
3. Add Phase 9 (US7 - Role Assignment) - 16 tasks → Validate independently
4. Complete Phase 10 (Polish) - 17 tasks → Final validation

**Total**: 130 tasks across 10 phases

**Each increment**:
- Adds specific user-facing value
- Is independently testable
- Doesn't break previous functionality
- Can be deployed/demoed separately

### Parallel Team Strategy (3+ Developers)

**Phase 1-2**: All developers collaborate on foundation (critical path)

**After Phase 2 Complete** (foundation ready):

**Team Split**:
- **Dev 1**: Phase 3 (US1) + Phase 4 (US2) - Infrastructure stories
- **Dev 2**: Phase 5 (US3) + Phase 6 (US4) - Security stories  
- **Dev 3**: Begin Phase 7 (US5) prep work once US4 complete

**After US1-US4 Complete**:
- **Dev 1**: Phase 8 (US6 - User Listing)
- **Dev 2**: Phase 9 (US7 - Role Assignment)
- **Dev 3**: Phase 10 (Polish) parallel work

---

## Task Summary

**Total Tasks**: 130

**Breakdown by Phase**:
- Phase 1 (Setup): 6 tasks
- Phase 2 (Foundational): 22 tasks ⚠️ BLOCKS ALL STORIES
- Phase 3 (US1 - Health Check): 3 tasks
- Phase 4 (US2 - Error Handling): 9 tasks
- Phase 5 (US3 - Feature Flags): 5 tasks
- Phase 6 (US4 - Authentication): 16 tasks
- Phase 7 (US5 - User CRUD): 19 tasks
- Phase 8 (US6 - User Listing): 17 tasks
- Phase 9 (US7 - Role Assignment): 16 tasks
- Phase 10 (Polish): 17 tasks

**Breakdown by User Story**:
- US1 (Health Check): 3 tasks - P1 Priority
- US2 (Error Handling): 9 tasks - P1 Priority
- US3 (Feature Flags): 5 tasks - P1 Priority
- US4 (Authentication): 16 tasks - P1 Priority
- US5 (User CRUD): 19 tasks - P2 Priority
- US6 (User Listing): 17 tasks - P2 Priority
- US7 (Role Assignment): 16 tasks - P2 Priority

**Parallelizable Tasks**: 74 tasks marked [P]

**MVP Scope** (US1-US4): 61 tasks
**Full Feature Set**: 130 tasks

---

## Validation Checklist

Before considering the feature complete:

- [ ] All 130 tasks completed (or consciously deferred)
- [ ] All tests pass: `./gradlew clean test`
- [ ] OpenAPI generation succeeds without errors
- [ ] All user stories independently tested per acceptance criteria
- [ ] Feature flag toggles work (verified both enabled/disabled states)
- [ ] Authentication flow works (login, token usage, bootstrap mode)
- [ ] Error responses consistent across all endpoints
- [ ] Password never appears in any response
- [ ] All endpoints respond within 1 second performance budget
- [ ] Database migrations apply cleanly
- [ ] Quickstart.md scenarios validated end-to-end
- [ ] Security review complete (no sensitive data leaks)
- [ ] Documentation up to date (README, JavaDocs)

---

## Notes

- **Lombok usage**: Apply @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor to all handwritten entity and service classes
- **Generated code**: Never edit files in src/main-gen/java - regenerate via `./gradlew openApiGenerate`
- **OpenAPI first**: src/main/resources/openapi.yaml is the source of truth
- **Feature flag pattern**: All users/auth endpoints gated by `FeatureFlag.usersApi` boolean property
- **Testing**: Every user story has mandatory test coverage (happy + negative scenarios)
- **Error codes**: Use ErrorCode enum, return consistent ErrorResponse with stable codes
- **Passwords**: Always hash with BCrypt, never serialize in responses (@JsonProperty WRITE_ONLY)
- **Bootstrap mode**: POST /users allowed without auth only when zero users exist
- **Pagination**: All list endpoints require page and pageSize parameters (1-100 range)

**Commit strategy**: Commit after completing each phase or logical task group for clean history and easy rollback.

---

**Generated**: 2025-12-27  
**Feature Directory**: F:\data\dev\SpecDriven\specs\main  
**Status**: Ready for implementation - Phase 2 (tasks) complete ✅

