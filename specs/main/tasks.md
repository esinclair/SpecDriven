# Tasks: User Management API System

**Feature**: User Management API System  
**Input**: Design documents from `/specs/main/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/openapi.contract.yaml ✅

**Spring-first development**: Tasks use Spring Boot annotations and capabilities (dependency injection, `@Valid` validation, `@ControllerAdvice` exception handling, Spring Data repositories, Spring Security, `@Transactional`, etc.) rather than custom solutions.

**Tests**: Tests are MANDATORY. Tasks include unit + integration tests for happy paths AND negative/error flows (invalid inputs, error responses, exception paths), aligned with the OpenAPI contract and HTTP status semantics.

**Feature flags (mandatory)**: Tasks include implementing a Spring boolean property flag named `FeatureFlag.usersApi` to gate the behavior (`true` enables, `false` disables), with default value `false`, and tests for both enabled and disabled modes.

**Paged results (mandatory)**: List users endpoint includes paged response shape + required pagination inputs in OpenAPI, plus tests for paging boundaries (empty page, last page, max page size).

**Public API compatibility (mandatory)**: No explicit API versioning (no `/v1`, headers, query params, or media type versioning). All changes are backward compatible and additive.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, build configuration, and basic structure

- [X] T001 Verify Spring Boot project structure exists with src/main/java, src/main/resources, src/test/java
- [X] T002 Configure build.gradle with Spring Boot 3.5.9, Spring Web, Spring Data JDBC, Spring Security, JWT (jjwt 0.12.3), Flyway, H2, OpenAPI Generator 7.14.0
- [X] T003 [P] Setup OpenAPI Generator Gradle plugin to generate code from src/main/resources/openapi.yaml to src/main-gen/java
- [X] T004 [P] Copy contracts/openapi.contract.yaml to src/main/resources/openapi.yaml
- [X] T005 [P] Create application.yml in src/main/resources with database config (H2 in PostgreSQL mode), feature flag config (FeatureFlag.usersApi: false), JWT config, server config
- [X] T006 [P] Create application-test.yml in src/test/resources with test-specific configuration (H2, feature flag overrides)
- [X] T007 Run ./gradlew openApiGenerate to verify code generation works and produces interfaces in src/main-gen/java
- [X] T008 Create SpecDrivenApplication.java main class in src/main/java/com/example/specdriven/SpecDrivenApplication.java with @SpringBootApplication

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database & Migrations

- [X] T009 Create Flyway migration V001__init_schema.sql in src/main/resources/db/migration creating users table with columns: id (UUID PK), username (VARCHAR 100), name (VARCHAR 255), email_address (VARCHAR 255 UNIQUE), password_hash (VARCHAR 255), created_at (TIMESTAMP), updated_at (TIMESTAMP), plus indexes on email_address and username
- [X] T010 [P] Create Flyway migration V002__roles_permissions.sql in src/main/resources/db/migration creating roles table, permissions table, role_permissions join table, and inserting predefined roles (ADMIN, USER, GUEST) and permissions (users:read, users:write, users:delete, roles:assign)
- [X] T011 [P] Create Flyway migration V003__users_api.sql in src/main/resources/db/migration creating user_roles join table with columns: user_id (UUID FK), role_id (UUID FK), assigned_at (TIMESTAMP), plus composite PK and indexes

### Domain Entities

- [X] T012 [P] Create UserEntity.java in src/main/java/com/example/specdriven/domain/UserEntity.java with Spring Data JDBC annotations (@Table, @Id, @Column) for users table
- [X] T013 [P] Create RoleEntity.java in src/main/java/com/example/specdriven/domain/RoleEntity.java with annotations for roles table
- [X] T014 [P] Create PermissionEntity.java in src/main/java/com/example/specdriven/domain/PermissionEntity.java with annotations for permissions table
- [X] T015 [P] Create UserRoleEntity.java in src/main/java/com/example/specdriven/domain/UserRoleEntity.java with composite key annotations for user_roles join table
- [X] T016 [P] Create RolePermissionEntity.java in src/main/java/com/example/specdriven/domain/RolePermissionEntity.java with composite key annotations for role_permissions join table

### Repositories

- [X] T017 [P] Create UserRepository.java interface in src/main/java/com/example/specdriven/repository/UserRepository.java extending PagingAndSortingRepository<UserEntity, UUID> with derived query methods: findByEmailAddress (Spring will generate query from method name)
- [X] T018 [P] Create RoleRepository.java interface in src/main/java/com/example/specdriven/repository/RoleRepository.java extending CrudRepository<RoleEntity, UUID> with findByRoleName method
- [X] T019 [P] Create UserRoleRepository.java interface in src/main/java/com/example/specdriven/repository/UserRoleRepository.java extending CrudRepository<UserRoleEntity, ?> with findByUserId, deleteByUserIdAndRoleId methods

### Exception Framework

- [X] T020 [P] Create ValidationException.java in src/main/java/com/example/specdriven/exception/ValidationException.java extending RuntimeException
- [X] T021 [P] Create ResourceNotFoundException.java in src/main/java/com/example/specdriven/exception/ResourceNotFoundException.java extending RuntimeException
- [X] T022 [P] Create ConflictException.java in src/main/java/com/example/specdriven/exception/ConflictException.java extending RuntimeException
- [X] T023 [P] Create AuthenticationException.java in src/main/java/com/example/specdriven/exception/AuthenticationException.java extending RuntimeException
- [X] T024 Create ErrorResponseFactory.java in src/main/java/com/example/specdriven/exception/ErrorResponseFactory.java with static methods to create ErrorResponse objects with stable codes: VALIDATION_FAILED, RESOURCE_NOT_FOUND, CONFLICT, AUTHENTICATION_REQUIRED, AUTHENTICATION_FAILED, INTERNAL_ERROR, SERVICE_UNAVAILABLE
- [X] T025 Create GlobalExceptionHandler.java in src/main/java/com/example/specdriven/exception/GlobalExceptionHandler.java with @ControllerAdvice and @ExceptionHandler methods mapping exceptions to HTTP status codes and ErrorResponse objects

### Security Configuration

- [X] T026 Create JwtConfig.java in src/main/java/com/example/specdriven/config/JwtConfig.java with @ConfigurationProperties("jwt") for secret and expirationMs
- [X] T027 Create SecurityConfig.java in src/main/java/com/example/specdriven/config/SecurityConfig.java with @Configuration @EnableWebSecurity configuring SecurityFilterChain to permit /ping, /login without auth and require auth for /users/**
- [X] T028 [P] Create PasswordEncoderConfig.java in src/main/java/com/example/specdriven/config/PasswordEncoderConfig.java with @Configuration and @Bean for BCryptPasswordEncoder

### Feature Flag Infrastructure

- [X] T029 Create FeatureFlagConfig.java in src/main/java/com/example/specdriven/config/FeatureFlagConfig.java with @ConfigurationProperties("FeatureFlag") containing boolean usersApi field with default false
- [X] T030 Create FeatureFlagService.java in src/main/java/com/example/specdriven/service/FeatureFlagService.java with @Service injecting FeatureFlagConfig and methods to check feature states

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - System Health Monitoring (Priority: P1) 🎯 MVP

**Goal**: Provide lightweight health check endpoint that requires no authentication and has no dependencies

**Independent Test**: Call GET /ping and verify 200 response with {"message": "pong"} without any authentication or database setup

### Tests for User Story 1 (MANDATORY)

- [X] T031 [P] [US1] Create HealthCheckIntegrationTest.java in src/test/java/com/example/specdriven/integration/HealthCheckIntegrationTest.java with @SpringBootTest testing GET /ping returns 200 with PingResponse message
- [X] T032 [P] [US1] Add test in HealthCheckIntegrationTest.java: ping_NoAuthRequired_Returns200 verifying health check works without Authorization header
- [X] T033 [P] [US1] Add test in HealthCheckIntegrationTest.java: ping_FeatureFlagDisabled_StillReturns200 verifying health check works even when FeatureFlag.usersApi is false
- [X] T034 [P] [US1] Add test in HealthCheckIntegrationTest.java: ping_ResponseTime_Under1Second verifying 95% of requests complete in under 1 second

### Implementation for User Story 1

- [X] T035 [US1] Create PingController.java in src/main/java/com/example/specdriven/controller/PingController.java implementing generated PingApi interface with ping() method returning PingResponse with message "pong"
- [X] T036 [US1] Update SecurityConfig.java to explicitly permit /ping endpoint without authentication (configure in SecurityFilterChain)
- [X] T037 [US1] Ensure feature flag filter (to be implemented) bypasses /ping endpoint

**Checkpoint**: At this point, User Story 1 should be fully functional - health check endpoint works without auth or DB

---

## Phase 4: User Story 7+8 - Consistent Error Handling (Priority: P1)

**Goal**: All error responses follow consistent format with stable error codes; HTTP status codes indicate retry behavior (4xx = don't retry, 5xx = may retry)

**Independent Test**: Trigger various error conditions and verify all return ErrorResponse with code and message fields, correct HTTP status codes

### Tests for User Story 7+8 (MANDATORY)

- [X] T038 [P] [US7] Create ErrorHandlingIntegrationTest.java in src/test/java/com/example/specdriven/integration/ErrorHandlingIntegrationTest.java testing validation error returns 400 with VALIDATION_FAILED code
- [X] T039 [P] [US7] Add test in ErrorHandlingIntegrationTest.java: notFound_Returns404WithResourceNotFoundCode testing 404 errors return RESOURCE_NOT_FOUND code
- [X] T040 [P] [US7] Add test in ErrorHandlingIntegrationTest.java: conflict_Returns409WithConflictCode testing 409 errors return CONFLICT code
- [X] T041 [P] [US7] Add test in ErrorHandlingIntegrationTest.java: authenticationFailure_Returns401WithAuthenticationFailedCode testing 401 errors return AUTHENTICATION_FAILED code
- [X] T042 [P] [US8] Add test in ErrorHandlingIntegrationTest.java: errorResponse_NoRetryableField verifying error responses don't contain retryable field
- [X] T043 [P] [US8] Add test in ErrorHandlingIntegrationTest.java: serviceUnavailable_Returns503 testing transient errors return 503 with SERVICE_UNAVAILABLE code

### Implementation for User Story 7+8

- [X] T044 [US7] Complete GlobalExceptionHandler.java with all @ExceptionHandler methods: ValidationException → 400, ResourceNotFoundException → 404, ConflictException → 409, AuthenticationException → 401, DataAccessException → 503, Exception → 500
- [X] T045 [US7] Verify ErrorResponseFactory.java generates stable error codes and safe messages (no stack traces, no sensitive data)
- [X] T046 [US8] Add documentation comments in GlobalExceptionHandler.java explaining retry semantics: 4xx = don't retry, 5xx = may retry
- [X] T047 [US7] Update GlobalExceptionHandler.java to optionally include Retry-After header for 503 responses

**Checkpoint**: Error handling is now consistent across all endpoints with stable codes and HTTP retry semantics

---

## Phase 5: User Story 2 - User Authentication (Priority: P1)

**Goal**: Enable users to authenticate with credentials and receive JWT bearer token for accessing protected resources

**Independent Test**: POST /login with valid credentials returns 200 with token; using token in Authorization header allows access to protected endpoints

### Tests for User Story 2 (MANDATORY)

- [X] T048 [P] [US2] Create LoginIntegrationTest.java in src/test/java/com/example/specdriven/integration/LoginIntegrationTest.java with @SpringBootTest
- [X] T049 [P] [US2] Add test in LoginIntegrationTest.java: login_ValidCredentials_ReturnsToken creating a user, logging in, and verifying 200 response with token
- [X] T050 [P] [US2] Add test in LoginIntegrationTest.java: login_ValidToken_AllowsAccessToProtectedEndpoint verifying token works for authenticated requests
- [X] T051 [P] [US2] Add test in LoginIntegrationTest.java: login_InvalidPassword_Returns400 testing wrong password returns 400 with AUTHENTICATION_FAILED
- [X] T052 [P] [US2] Add test in LoginIntegrationTest.java: login_UnknownUsername_Returns400WithSameError verifying unknown username returns same error as wrong password (non-enumeration)
- [X] T053 [P] [US2] Add test in LoginIntegrationTest.java: login_MissingFields_Returns400ValidationFailed testing missing required fields returns VALIDATION_FAILED
- [X] T054 [P] [US2] Add test in LoginIntegrationTest.java: protectedEndpoint_NoToken_Returns401 verifying requests without token return 401 AUTHENTICATION_REQUIRED
- [X] T055 [P] [US2] Add test in LoginIntegrationTest.java: protectedEndpoint_ExpiredToken_Returns401 verifying expired tokens return 401 AUTHENTICATION_FAILED
- [X] T056 [P] [US2] Add test in LoginIntegrationTest.java: protectedEndpoint_MalformedToken_Returns401 verifying malformed Authorization header returns 401
- [X] T057 [P] [US2] Create JwtTokenProviderTest.java in src/test/java/com/example/specdriven/security/JwtTokenProviderTest.java with unit tests for token generation and validation

### Implementation for User Story 2

- [X] T058 [P] [US2] Create JwtTokenProvider.java in src/main/java/com/example/specdriven/security/JwtTokenProvider.java with @Component, methods generateToken(userId), validateToken(token), getUserIdFromToken(token) using jjwt library
- [X] T059 [US2] Create JwtAuthenticationFilter.java in src/main/java/com/example/specdriven/security/JwtAuthenticationFilter.java extending OncePerRequestFilter to extract Bearer token, validate with JwtTokenProvider, set Spring Security authentication context
- [X] T060 [US2] Update SecurityConfig.java to add JwtAuthenticationFilter to filter chain before UsernamePasswordAuthenticationFilter
- [X] T061 [US2] Create LoginService.java in src/main/java/com/example/specdriven/service/LoginService.java with @Service @Transactional methods: login(LoginRequest) → LoginResponse validating credentials with BCrypt and returning JWT token
- [X] T062 [US2] Implement non-enumeration in LoginService.java: always hash password even if user not found, return same error for unknown username and wrong password
- [X] T063 [US2] Create LoginController.java in src/main/java/com/example/specdriven/controller/LoginController.java implementing generated LoginApi interface, delegating to LoginService
- [X] T064 [US2] Update SecurityConfig.java to permit /login endpoint without authentication

**Checkpoint**: Authentication is fully functional - users can login and use tokens for protected endpoints

---

## Phase 6: User Story 3 - Create and Retrieve Users (Priority: P1)

**Goal**: Enable creating new users with validation and retrieving user details by ID; always require authentication for user creation

**Independent Test**: Create user with valid data returns 201 with user object; retrieve user by ID returns 200 with user details including roles; password not in response

### Tests for User Story 3 (MANDATORY)

- [X] T065 [P] [US3] Create UserCrudIntegrationTest.java in src/test/java/com/example/specdriven/integration/UserCrudIntegrationTest.java with @SpringBootTest
- [X] T066 [P] [US3] Add test in UserCrudIntegrationTest.java: createUser_ValidData_Returns201 testing authenticated user creation returns 201 with User object including ID
- [X] T067 [P] [US3] Add test in UserCrudIntegrationTest.java: createUser_PasswordNotInResponse verifying password field not returned in User response
- [X] T068 [P] [US3] Add test in UserCrudIntegrationTest.java: getUserById_ValidId_Returns200 testing GET /users/{id} returns 200 with user details
- [X] T069 [P] [US3] Add test in UserCrudIntegrationTest.java: createUser_MissingRequiredFields_Returns400 testing validation failures return 400 VALIDATION_FAILED
- [X] T070 [P] [US3] Add test in UserCrudIntegrationTest.java: createUser_InvalidEmailFormat_Returns400 testing invalid email format returns 400 VALIDATION_FAILED
- [X] T071 [P] [US3] Add test in UserCrudIntegrationTest.java: createUser_DuplicateEmail_Returns409 testing duplicate email returns 409 CONFLICT
- [X] T072 [P] [US3] Add test in UserCrudIntegrationTest.java: getUserById_NotFound_Returns404 testing non-existent user ID returns 404 RESOURCE_NOT_FOUND
- [X] T073 [P] [US3] Add test in UserCrudIntegrationTest.java: createUser_NoAuth_Returns401 testing user creation without auth returns 401
- [X] T074 [P] [US3] Add test in UserCrudIntegrationTest.java: getUserById_NoAuth_Returns401 testing GET without auth returns 401
- [X] T075 [P] [US3] Add test in UserCrudIntegrationTest.java: createUser_DatabaseUnavailable_Returns503 testing transient DB failure returns 503 SERVICE_UNAVAILABLE
- [X] T076 [P] [US3] Create UserServiceTest.java in src/test/java/com/example/specdriven/service/UserServiceTest.java with unit tests for service layer logic using Mockito
- [X] T077 [P] [US3] Create UserMapperTest.java in src/test/java/com/example/specdriven/mapper/UserMapperTest.java with unit tests for DTO/entity conversions

### Implementation for User Story 3

- [X] T078 [P] [US3] Create UserMapper.java in src/main/java/com/example/specdriven/mapper/UserMapper.java with @Component methods: toEntity(CreateUserRequest), toDto(UserEntity, List<RoleEntity>), toEntity(UpdateUserRequest, UserEntity) - hash passwords with BCryptPasswordEncoder, never map password to DTO
- [X] T079 [US3] Create UserService.java in src/main/java/com/example/specdriven/service/UserService.java with @Service @Transactional methods: createUser(CreateUserRequest) → User, getUserById(UUID) → User, validateEmailUniqueness
- [X] T080 [US3] Implement email uniqueness validation in UserService.java: check UserRepository.findByEmail before create, throw ConflictException if exists
- [X] T081 [US3] Implement password hashing in UserMapper.java: call passwordEncoder.encode() when mapping CreateUserRequest to UserEntity
- [X] T082 [US3] Create UsersController.java in src/main/java/com/example/specdriven/controller/UsersController.java implementing generated UsersApi interface with createUser() and getUserById() methods delegating to UserService
- [X] T083 [US3] Ensure UsersController validates inputs using @Valid annotation on request parameters
- [X] T084 [US3] Load user's roles in UserService.getUserById() by querying UserRoleRepository and RoleRepository, pass to UserMapper.toDto()

**Checkpoint**: Users can be created with authentication and retrieved with full role information, passwords are hashed and never returned

---

## Phase 7: User Story 4 - Update and Delete Users (Priority: P1)

**Goal**: Enable updating user information and deleting users; maintain data integrity with cascade deletes for role assignments

**Independent Test**: Update user with valid data returns 200 with updated user; delete user returns 204; subsequent GET returns 404

### Tests for User Story 4 (MANDATORY)

- [X] T089 [P] [US4] Add test in UserCrudIntegrationTest.java: updateUser_ValidData_Returns200 testing PUT /users/{id} with valid data returns 200 with updated User
- [X] T090 [P] [US4] Add test in UserCrudIntegrationTest.java: updateUser_PartialUpdate_UpdatesOnlyProvidedFields testing partial updates work correctly
- [X] T091 [P] [US4] Add test in UserCrudIntegrationTest.java: updateUser_PasswordChange_HashesNewPassword testing password update works and is hashed
- [X] T092 [P] [US4] Add test in UserCrudIntegrationTest.java: deleteUser_ValidId_Returns204 testing DELETE /users/{id} returns 204
- [X] T093 [P] [US4] Add test in UserCrudIntegrationTest.java: deleteUser_VerifyDeleted_Returns404 testing GET after DELETE returns 404
- [X] T094 [P] [US4] Add test in UserCrudIntegrationTest.java: deleteUser_CascadesRoleAssignments verifying role assignments deleted when user deleted
- [X] T095 [P] [US4] Add test in UserCrudIntegrationTest.java: updateUser_InvalidData_Returns400 testing invalid field values return 400 VALIDATION_FAILED
- [X] T096 [P] [US4] Add test in UserCrudIntegrationTest.java: updateUser_NotFound_Returns404 testing update non-existent user returns 404 RESOURCE_NOT_FOUND
- [X] T097 [P] [US4] Add test in UserCrudIntegrationTest.java: deleteUser_NotFound_Returns404 testing delete non-existent user returns 404 RESOURCE_NOT_FOUND
- [X] T098 [P] [US4] Add test in UserCrudIntegrationTest.java: updateUser_DuplicateEmail_Returns409 testing email conflict on update returns 409 CONFLICT
- [X] T099 [P] [US4] Add test in UserCrudIntegrationTest.java: updateUser_NoAuth_Returns401 testing update without auth returns 401
- [X] T100 [P] [US4] Add test in UserCrudIntegrationTest.java: deleteUser_NoAuth_Returns401 testing delete without auth returns 401

### Implementation for User Story 4

- [X] T101 [US4] Add updateUser(UUID, UpdateUserRequest) method to UserService.java: fetch existing user, validate uniqueness if email changed, apply updates via UserMapper, save updated entity, return User DTO
- [X] T102 [US4] Add deleteUser(UUID) method to UserService.java: check user exists, delete from UserRepository (cascade deletes user_roles automatically via DB FK constraint)
- [X] T103 [US4] Update UserMapper.java with updateEntity(UpdateUserRequest, UserEntity) method for partial updates: only update non-null fields, hash password if provided
- [X] T104 [US4] Implement updateUser() and deleteUser() methods in UsersController.java delegating to UserService with @Valid validation
- [X] T105 [US4] Update updated_at timestamp in UserEntity when updating via UserService (set to current timestamp)
- [X] T106 [US4] Verify Flyway migration includes ON DELETE CASCADE for user_roles.user_id foreign key to ensure role assignments are deleted

**Checkpoint**: Users can be updated with partial data and deleted; role assignments cascade delete; all validations work

---

## Phase 8: User Story 5 - List and Filter Users (Priority: P2)

**Goal**: Enable retrieving paginated list of users with optional filters; return pagination metadata; enforce mandatory pagination parameters

**Independent Test**: GET /users?page=1&pageSize=10 returns 200 with UserPage including items array and pagination metadata (totalCount, totalPages); filters work correctly

### Tests for User Story 5 (MANDATORY)

- [X] T107 [P] [US5] Create UserListIntegrationTest.java in src/test/java/com/example/specdriven/integration/UserListIntegrationTest.java with @SpringBootTest
- [X] T108 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_ValidPagination_Returns200WithPage testing GET /users with page and pageSize returns UserPage with items and metadata
- [X] T109 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_MultiplePages_ReturnsCorrectPage testing pagination returns correct page of results with correct totalCount and totalPages
- [X] T110 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_LastPage_ReturnsRemainingItems testing last page contains remaining items (less than pageSize)
- [X] T111 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_EmptyResults_ReturnsEmptyPageWith200 testing query with no matches returns 200 with empty items array and valid metadata
- [X] T112 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_FilterByUsername_ReturnsMatchingUsers testing username filter returns only matching users
- [X] T113 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_FilterByEmail_ReturnsMatchingUsers testing emailAddress filter returns exact match
- [X] T114 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_FilterByName_ReturnsCaseInsensitivePartialMatch testing name filter works case-insensitively
- [X] T115 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_FilterByRoleName_ReturnsUsersWithRole testing roleName filter returns users with that role assigned
- [X] T116 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_MultipleFilters_ReturnsUsersMatchingAll testing AND logic when multiple filters provided
- [X] T117 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_MissingPagination_Returns400 testing missing page or pageSize parameters returns 400 VALIDATION_FAILED
- [X] T118 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_InvalidPagination_Returns400 testing invalid page/pageSize (negative, zero, >100) returns 400 VALIDATION_FAILED
- [X] T119 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_UnsupportedQueryParam_Returns400 testing unknown query parameters return 400 VALIDATION_FAILED
- [X] T120 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_NoAuth_Returns401 testing list without auth returns 401
- [X] T121 [P] [US5] Add test in UserListIntegrationTest.java: listUsers_MaxPageSize_EnforcesLimit testing pageSize > 100 is rejected

### Implementation for User Story 5

- [X] T122 [US5] Use Spring Data's PagingAndSortingRepository to add findAll(Pageable pageable) → Page<UserEntity> method (built-in paging support)
- [X] T123 [US5] Add derived query methods to UserRepository.java for filtering: findByUsername, findByEmailAddress, findByNameContainingIgnoreCase (Spring generates queries from method names), and use Pageable parameter for pagination
- [X] T124 [US5] Add listUsers(page, pageSize, filters) method to UserService.java: validate pagination params, create Pageable object using PageRequest.of(page-1, pageSize), call repository.findAll(Pageable) or filtered methods with Pageable parameter, map Page<UserEntity> to UserPage DTO with totalCount, totalPages from Page object
- [X] T125 [US5] Implement filter logic in UserService.java: use appropriate Spring Data derived query methods based on provided filters (call findByUsername, findByEmailAddress, findByNameContainingIgnoreCase with Pageable parameter; for role filtering, coordinate with UserRoleRepository)
- [X] T126 [US5] Implement listUsers() method in UsersController.java: validate pagination parameters with @Valid, delegate to UserService, return UserPage response
- [X] T127 [US5] Add validation in UsersController or service to enforce pageSize maximum of 100 and minimum of 1, page minimum of 1
- [X] T128 [US5] Add validation to reject unsupported query parameters not defined in OpenAPI contract

**Checkpoint**: List users endpoint works with pagination and all filters; validation enforces required params and limits

---

## Phase 9: User Story 6 - Manage User Roles (Priority: P2)

**Goal**: Enable assigning and removing roles from users with idempotent operations; validate role names against predefined set

**Independent Test**: PUT /users/{id}/roles/{roleName} returns 204; GET /users/{id} shows role in roles array; DELETE /users/{id}/roles/{roleName} returns 204; subsequent GET shows role removed; repeated assign/remove are idempotent

### Tests for User Story 6 (MANDATORY)

- [X] T129 [P] [US6] Create RoleManagementIntegrationTest.java in src/test/java/com/example/specdriven/integration/RoleManagementIntegrationTest.java with @SpringBootTest
- [X] T130 [P] [US6] Add test in RoleManagementIntegrationTest.java: assignRole_ValidRole_Returns204 testing PUT /users/{id}/roles/{roleName} returns 204
- [X] T131 [P] [US6] Add test in RoleManagementIntegrationTest.java: assignRole_VerifyAssigned_RoleAppearsInUser testing assigned role appears in GET /users/{id} response
- [X] T132 [P] [US6] Add test in RoleManagementIntegrationTest.java: assignRole_AlreadyAssigned_IdempotentReturns204 testing assigning same role twice returns 204 both times (idempotent)
- [X] T133 [P] [US6] Add test in RoleManagementIntegrationTest.java: removeRole_ValidRole_Returns204 testing DELETE /users/{id}/roles/{roleName} returns 204
- [X] T134 [P] [US6] Add test in RoleManagementIntegrationTest.java: removeRole_VerifyRemoved_RoleNotInUser testing removed role no longer appears in GET /users/{id} response
- [X] T135 [P] [US6] Add test in RoleManagementIntegrationTest.java: removeRole_NotAssigned_IdempotentReturns204 testing removing unassigned role returns 204 (idempotent)
- [X] T136 [P] [US6] Add test in RoleManagementIntegrationTest.java: assignRole_UserNotFound_Returns404 testing assign role to non-existent user returns 404 RESOURCE_NOT_FOUND
- [X] T137 [P] [US6] Add test in RoleManagementIntegrationTest.java: assignRole_InvalidRoleName_Returns400 testing invalid role name returns 400 VALIDATION_FAILED
- [X] T138 [P] [US6] Add test in RoleManagementIntegrationTest.java: assignRole_NoAuth_Returns401 testing assign without auth returns 401
- [X] T139 [P] [US6] Add test in RoleManagementIntegrationTest.java: removeRole_NoAuth_Returns401 testing remove without auth returns 401
- [X] T140 [P] [US6] Create RoleServiceTest.java in src/test/java/com/example/specdriven/service/RoleServiceTest.java with unit tests for role assignment logic

### Implementation for User Story 6

- [X] T141 [P] [US6] Create RoleMapper.java in src/main/java/com/example/specdriven/mapper/RoleMapper.java with @Component methods: toDto(RoleEntity, List<PermissionEntity>) → Role
- [X] T142 [US6] Create RoleService.java in src/main/java/com/example/specdriven/service/RoleService.java with @Service @Transactional methods: assignRole(UUID userId, String roleName), removeRole(UUID userId, String roleName)
- [X] T143 [US6] Implement assignRole in RoleService.java: validate user exists (throw ResourceNotFoundException), validate role exists by roleName (throw ValidationException), check if already assigned, if not create UserRoleEntity and save (idempotent)
- [X] T144 [US6] Implement removeRole in RoleService.java: validate user exists, find role by name, delete UserRoleEntity if exists, return success regardless (idempotent)
- [X] T145 [US6] Create UserRolesController.java in src/main/java/com/example/specdriven/controller/UserRolesController.java implementing generated UsersApi role endpoints: assignRoleToUser() and removeRoleFromUser() delegating to RoleService
- [X] T146 [US6] Add validation in RoleService to ensure roleName matches one of the predefined RoleNameEnum values (ADMIN, USER, GUEST)

**Checkpoint**: Role assignment and removal work with proper idempotency; validation prevents invalid roles; all tests pass

---

## Phase 10: User Story 9 - Feature Flag Control (Priority: P1)

**Goal**: All user management endpoints (except /ping) are gated by FeatureFlag.usersApi; when disabled endpoints return 404; tests verify both enabled and disabled states

**Independent Test**: With feature flag disabled, GET /users returns 404; with flag enabled, same request returns 200 (or 401 if not authenticated); /ping always works

### Tests for User Story 9 (MANDATORY)

- [X] T147 [P] [US9] Create FeatureFlagIntegrationTest.java in src/test/java/com/example/specdriven/integration/FeatureFlagIntegrationTest.java with @SpringBootTest
- [X] T148 [P] [US9] Add test in FeatureFlagIntegrationTest.java: usersApi_FeatureFlagDisabled_Returns404 testing all user endpoints (/users, /users/{id}, /login) return 404 when FeatureFlag.usersApi=false
- [X] T149 [P] [US9] Add test in FeatureFlagIntegrationTest.java: usersApi_FeatureFlagEnabled_ProcessesRequests testing user endpoints work normally when FeatureFlag.usersApi=true
- [X] T150 [P] [US9] Add test in FeatureFlagIntegrationTest.java: ping_FeatureFlagDisabled_StillWorks verifying /ping works regardless of feature flag state
- [X] T151 [P] [US9] Add test in FeatureFlagIntegrationTest.java: featureFlagDisabled_ErrorDoesNotRevealFeature verifying 404 error message doesn't reveal feature exists or is disabled
- [X] T152 [P] [US9] Create FeatureFlagSecurityFilterTest.java in src/test/java/com/example/specdriven/security/FeatureFlagSecurityFilterTest.java with unit tests for filter logic

### Implementation for User Story 9

- [X] T153 [US9] Create FeatureFlagSecurityFilter.java in src/main/java/com/example/specdriven/security/FeatureFlagSecurityFilter.java extending OncePerRequestFilter to check if request path requires feature flag, if FeatureFlag.usersApi is false and path is /users/** or /login, return 404 with RESOURCE_NOT_FOUND (no feature disclosure)
- [X] T154 [US9] Update SecurityConfig.java to add FeatureFlagSecurityFilter to filter chain before JwtAuthenticationFilter
- [X] T155 [US9] Configure FeatureFlagSecurityFilter to explicitly bypass /ping endpoint (always allow)
- [X] T156 [US9] Verify application.yml has FeatureFlag.usersApi set to false as default
- [X] T157 [US9] Add documentation comment in FeatureFlagConfig.java explaining default value is false until feature validated
- [X] T158 [US9] Update application-test.yml to set FeatureFlag.usersApi=true for most tests (except feature flag specific tests)

**Checkpoint**: Feature flag gates all user API endpoints; /ping bypasses flag; tests verify both modes

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories; final validation; documentation

### Integration & End-to-End Tests

- [X] T159 [P] Create FullUserLifecycleIntegrationTest.java in src/test/java/com/example/specdriven/integration/FullUserLifecycleIntegrationTest.java testing complete flow: create user → login → get token → assign role → update user → list users → delete user → verify deleted
- [X] T160 [P] Add test in FullUserLifecycleIntegrationTest.java: multipleUsers_IndependentOperations testing concurrent operations don't interfere
- [X] T161 [P] Add performance smoke test in HealthCheckIntegrationTest.java: verify95PercentUnder1Second making 100 requests and checking 95th percentile

### Repository Tests

- [X] T162 [P] Create UserRepositoryTest.java in src/test/java/com/example/specdriven/repository/UserRepositoryTest.java with @DataJdbcTest testing repository methods: save, findById, findByEmail, countUsers, findAllPaginated
- [X] T163 [P] Create UserRoleRepositoryTest.java in src/test/java/com/example/specdriven/repository/UserRoleRepositoryTest.java with @DataJdbcTest testing role assignment queries

### Code Quality

- [X] T164 [P] Review all exception handling to ensure no sensitive data (stack traces, internal IDs) exposed in error messages
- [X] T165 [P] Review all endpoints to ensure passwords never returned in responses
- [X] T166 [P] Verify all @Transactional boundaries are correct (service layer methods modifying data)
- [X] T167 [P] Add JavaDoc comments to public service methods explaining business logic and validation rules

### Documentation

- [X] T168 [P] Update README.md with quick start instructions: clone, build, enable feature flag, run, access /ping
- [X] T169 [P] Add API usage examples to README.md showing curl commands for common workflows (bootstrap, login, CRUD, roles)
- [X] T170 [P] Verify quickstart.md matches actual implementation and all examples work
- [X] T171 [P] Add comments in application.yml explaining each configuration property

### Final Validation

- [X] T172 Run ./gradlew clean build and verify all tests pass (unit + integration)
- [X] T173 Verify OpenAPI code regenerates cleanly with ./gradlew openApiGenerate (no manual edits to generated code)
- [X] T174 Start application with ./gradlew bootRun and manually test health check endpoint
- [ ] T175 Manually test authentication flow: login, get token, use token for CRUD operations
- [ ] T176 Manually test feature flag: disable FeatureFlag.usersApi, verify endpoints return 404, verify /ping still works
- [ ] T177 Run quickstart.md validation: follow all steps in quickstart.md and verify they work
- [ ] T178 Generate and review test coverage report to ensure adequate coverage of happy paths and negative scenarios
- [X] T179 Verify build artifact is created: build/libs/SpecDriven-0.0.1-SNAPSHOT.jar exists and is executable

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational phase - can start after Phase 2
- **User Story 7+8 (Phase 4)**: Depends on Foundational phase - completes error handling framework
- **User Story 2 (Phase 5)**: Depends on Foundational + US7+8 - requires error handling
- **User Story 3 (Phase 6)**: Depends on Foundational + US7+8 + US2 - requires auth and error handling
- **User Story 4 (Phase 7)**: Depends on Foundational + US7+8 + US2 + US3 - extends CRUD operations
- **User Story 5 (Phase 8)**: Depends on Foundational + US7+8 + US2 + US3 - adds listing/filtering
- **User Story 6 (Phase 9)**: Depends on Foundational + US7+8 + US2 + US3 - manages roles
- **User Story 9 (Phase 10)**: Depends on all other phases - gates all endpoints
- **Polish (Phase 11)**: Depends on all user story phases being complete

### User Story Dependencies

```
Setup (Phase 1)
    ↓
Foundational (Phase 2) [BLOCKING]
    ↓
    ├─→ US1: Health Check (Phase 3) [P1] - Independent, can start immediately after Phase 2
    ├─→ US7+8: Error Handling (Phase 4) [P1] - Independent, completes foundation
    │       ↓
    │   US2: Authentication (Phase 5) [P1] - Requires error handling
    │       ↓
    │   US3: Create/Retrieve Users (Phase 6) [P1] - Requires auth + error handling
    │       ↓
    │       ├─→ US4: Update/Delete Users (Phase 7) [P1] - Extends US3
    │       ├─→ US5: List/Filter Users (Phase 8) [P2] - Extends US3
    │       └─→ US6: Manage Roles (Phase 9) [P2] - Extends US3
    │               ↓
    └─→ US9: Feature Flags (Phase 10) [P1] - Gates all endpoints
            ↓
    Polish & Integration (Phase 11)
```

### Within Each User Story

1. **Tests first** (can be written in parallel if desired)
   - Contract tests
   - Integration tests (happy path)
   - Negative tests
   - Feature flag tests (if applicable)

2. **Implementation**
   - Models/Mappers (parallel if independent)
   - Services (depends on models)
   - Controllers (depends on services)
   - Integration points

3. **Story complete** before moving to next priority

### Parallel Opportunities

**Within Phase 1 (Setup)**: Tasks T003, T004, T005, T006, T008 can run in parallel

**Within Phase 2 (Foundational)**:
- T010, T011 (migrations) in parallel
- T012-T016 (entities) all in parallel
- T017-T019 (repositories) all in parallel
- T020-T023 (exceptions) all in parallel
- T027, T028 (security config) in parallel

**Within each User Story Phase**:
- All test creation tasks marked [P] can run in parallel
- Model/Mapper creation tasks marked [P] can run in parallel
- Implementation tasks must follow dependency order (models → services → controllers)

**Across User Story Phases** (if team capacity allows):
- After Phase 2 completes, Phase 3 (US1) can start immediately
- After Phase 4 completes, US2 can start
- Once US3 completes, US4, US5, US6 can potentially work in parallel if developers avoid conflicts

---

## Implementation Strategy

### MVP First (Minimum Viable Product)

**Goal**: Get basic system working end-to-end as quickly as possible

1. **Complete Phase 1**: Setup (T001-T008) - ~2-4 hours
2. **Complete Phase 2**: Foundational (T009-T030) - ~1 day
   - **CRITICAL**: Must complete all foundational infrastructure before proceeding
3. **Complete Phase 3**: US1 - Health Check (T031-T037) - ~2-3 hours
4. **Complete Phase 4**: US7+8 - Error Handling (T038-T047) - ~3-4 hours
5. **Complete Phase 5**: US2 - Authentication (T048-T064) - ~1 day
6. **Complete Phase 6**: US3 - Create/Retrieve Users (T065-T088) - ~1-2 days
7. **STOP and VALIDATE**: Test independently, verify all tests pass
8. **MVP Complete**: System can check health, authenticate, and manage users (create/retrieve)

**MVP Scope**: ~4-5 days of focused development
- Health check works
- Users can login and get tokens
- Users can be created with proper authentication and retrieved
- All tests pass
- Feature flag controls access

### Incremental Delivery

After MVP, add features incrementally:

1. **Phase 7**: US4 - Update/Delete (T089-T106) → ~1 day → **Release 1.1**
2. **Phase 8**: US5 - List/Filter (T107-T128) → ~1 day → **Release 1.2**
3. **Phase 9**: US6 - Roles (T129-T146) → ~1 day → **Release 1.3**
4. **Phase 10**: US9 - Feature Flags (T147-T158) → ~0.5 day → **Release 1.4**
5. **Phase 11**: Polish (T159-T180) → ~1-2 days → **Release 2.0**

Each release adds value without breaking previous functionality.

### Parallel Team Strategy

With 3 developers after Foundational phase completes:

- **Developer A**: US1 (Health) → US2 (Auth) → US3 (Create/Retrieve) → US9 (Flags)
- **Developer B**: US7+8 (Errors) → US4 (Update/Delete) → US5 (List/Filter)
- **Developer C**: Tests + Documentation → US6 (Roles) → Polish (Phase 11)

**Coordination points**:
- US2, US3, US4, US5, US6 all need error handling (US7+8) complete
- US3, US4, US5, US6 all need authentication (US2) complete
- Developer coordination on UserService.java to avoid merge conflicts

---

## Summary

**Total Tasks**: 180 tasks organized into 11 phases

**Task Breakdown by Phase**:
- Phase 1 (Setup): 8 tasks
- Phase 2 (Foundational): 22 tasks
- Phase 3 (US1 - Health): 7 tasks
- Phase 4 (US7+8 - Errors): 10 tasks
- Phase 5 (US2 - Auth): 17 tasks
- Phase 6 (US3 - Create/Retrieve): 24 tasks
- Phase 7 (US4 - Update/Delete): 18 tasks
- Phase 8 (US5 - List/Filter): 21 tasks
- Phase 9 (US6 - Roles): 18 tasks
- Phase 10 (US9 - Feature Flags): 12 tasks
- Phase 11 (Polish): 23 tasks

**Parallel Opportunities**:
- ~60 tasks marked [P] can run in parallel within their phase
- Multiple user story phases can overlap if team capacity allows (after dependencies met)

**Independent Test Criteria**:
- Each user story has explicit tests proving it works independently
- All tests are mandatory (happy path + negative scenarios)
- Feature flag tests verify enabled/disabled states
- Pagination tests verify boundaries and metadata

**MVP Scope**: Phases 1-6 (US1, US7+8, US2, US3) = ~77 tasks = Core user management with auth

**Constitution Compliance**: ✅ All requirements addressed
- Tests mandatory: ✅ Every user story has comprehensive tests
- Paged results: ✅ US5 implements mandatory pagination with tests
- Error contract: ✅ US7+8 implements stable codes with HTTP status semantics
- No versioning: ✅ No version paths, headers, or params
- Feature flags: ✅ US9 implements FeatureFlag.usersApi with tests
- Build gate: ✅ Phase 11 validates ./gradlew clean build

---

**Tasks Status**: ✅ Complete and Ready for Implementation  
**Generated**: 2025-12-30  
**Next Step**: Begin Phase 1 (Setup) or review with team to adjust priorities
