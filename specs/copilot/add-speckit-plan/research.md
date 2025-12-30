# Research: User Management API System

**Feature**: User Management API System | **Date**: 2025-12-30 | **Spec**: [spec.md](./spec.md)

## Overview

This document captures the technical research and decision-making process for implementing the User Management API System. All decisions are based on the requirements in spec.md and align with the SpecDriven constitution principles.

## Technology Stack Decisions

### Primary Framework: Spring Boot 3.5.9

**Decision**: Use Spring Boot 3.5.9 (latest stable 3.5.x series)

**Rationale**:
- Aligns with specified constraint in prompt: "demonstrating OpenAPI-first development with Spring Boot 3.5.x and JDK 17"
- Spring Boot 3.x series uses Jakarta EE (jakarta.* packages) required for modern Java
- Version 3.5.9 is the latest stable release
- Built-in support for OpenAPI integration via annotations and code generation
- Comprehensive testing support with Spring Boot Test
- Mature ecosystem for REST API development
- Constitution Principle 5 requires Spring-First Development

**Alternatives Considered**:
- **Spring Boot 2.x**: Rejected - older javax.* namespace; not requested version
- **Micronaut**: Rejected - would require learning new framework; Spring Boot specified
- **Quarkus**: Rejected - would require different build/runtime model; Spring Boot specified

**Implementation Details**:
- Use Spring Initializr conventions for project structure
- Leverage Spring Boot starters: web, data-jdbc, security, validation
- Use Spring Boot auto-configuration for database, security, and web layers
- Follow constitution requirement for Spring annotations and capabilities

---

### Java Version: JDK 17

**Decision**: Use Java 17 (LTS)

**Rationale**:
- Aligns with specified constraint in prompt: "demonstrating OpenAPI-first development with Spring Boot 3.5.x and JDK 17"
- Java 17 is a Long-Term Support (LTS) release
- Required for Spring Boot 3.5.x (minimum Java 17)
- Provides modern language features: records, pattern matching, sealed classes, text blocks
- Wide tooling and IDE support
- Production-ready and stable

**Alternatives Considered**:
- **Java 21 (LTS)**: Rejected - requirement specifies JDK 17
- **Java 11 (LTS)**: Rejected - incompatible with Spring Boot 3.x
- **Java 8**: Rejected - too old; not compatible with Spring Boot 3.x

**Implementation Details**:
- Configure Gradle toolchain to use Java 17: `java.toolchain.languageVersion = JavaLanguageVersion.of(17)`
- Use modern Java features where appropriate (text blocks for SQL, records for immutable DTOs)
- Leverage Java 17 features for cleaner code (pattern matching, switch expressions)

---

### Build Tool: Gradle

**Decision**: Use Gradle with Gradle Wrapper

**Rationale**:
- Constitution requires Gradle as the single source of build truth (Principle 5)
- Required gate: `./gradlew clean build` must pass
- Existing repository uses Gradle (build.gradle, settings.gradle present)
- Gradle has excellent OpenAPI code generation plugin support
- Supports Java 17 toolchain configuration
- Build caching and incremental compilation improve developer productivity

**Alternatives Considered**:
- **Maven**: Rejected - constitution specifies Gradle
- **Bazel**: Rejected - overly complex for this project; Gradle required

**Implementation Details**:
- Use Gradle wrapper (`./gradlew`) for consistent build experience
- Configure OpenAPI Generator Gradle plugin for code generation
- Define separate source sets for generated code
- Ensure generated code is not committed to version control

---

### Database: H2 In-Memory (SQL-Compliant Mode)

**Decision**: Use H2 in-memory database in PostgreSQL compatibility mode

**Rationale**:
- Zero external dependencies for development and testing
- Fast startup and teardown for tests (supports performance budget)
- PostgreSQL mode ensures SQL standards compliance (no H2-specific syntax)
- Supports migrations that can work with production databases (PostgreSQL, MySQL)
- Embedded mode suitable for demo and testing purposes
- Aligns with constitution performance budget (≤1s response time requirement)

**Alternatives Considered**:
- **PostgreSQL**: Rejected - requires external database server; adds setup complexity
- **MySQL**: Rejected - requires external database server; adds setup complexity
- **SQLite**: Rejected - limited SQL feature support
- **H2 default mode**: Rejected - uses non-standard SQL

**Implementation Details**:
- Connection URL: `jdbc:h2:mem:usermgmt;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`
- MODE=PostgreSQL enables SQL standards compliance
- DB_CLOSE_DELAY=-1 keeps database alive until JVM shutdown
- Enable H2 console for debugging: `/h2-console` (development only)
- Use in-memory for fast test execution

---

### Data Access: Spring Data JDBC

**Decision**: Use Spring Data JDBC repositories

**Rationale**:
- Aligns with Constitution Principle 5: Spring-First Development
- Constitution requires "Use Spring Data repositories for database operations"
- Simpler than JPA: no lazy loading, no session management, no proxy objects
- Explicit control over SQL queries (better performance transparency)
- Better performance for simple CRUD operations (meets ≤1s budget)
- No N+1 query problems (common with JPA)
- Lightweight and fits the application's needs (no complex object graphs)

**Alternatives Considered**:
- **Spring Data JPA**: Rejected - adds unnecessary complexity for simple CRUD operations
- **JDBC Template**: Rejected - more boilerplate; constitution prefers Spring Data repositories
- **MyBatis**: Rejected - constitution requires Spring-first approach

**Implementation Details**:
- Repositories extend `CrudRepository<Entity, ID>`
- Use `@Query` annotation for custom queries (filtering, pagination)
- Entity classes use `@Table`, `@Id`, and `@Column` annotations
- No lazy loading or complex relationships
- Leverage Spring Data JDBC's automatic ID generation

---

### Database Migrations: Flyway

**Decision**: Use Flyway for database schema versioning

**Rationale**:
- Industry standard for database migrations
- First-class support in Spring Boot
- Simple SQL-based migrations (no DSL to learn)
- Automatic migration on application startup
- Supports multiple database vendors (H2, PostgreSQL, MySQL)
- Versioned migrations prevent inconsistencies across environments
- Aligns with Spring-First Development principle

**Alternatives Considered**:
- **Liquibase**: Rejected - XML/YAML config adds complexity; Flyway is simpler
- **Manual schema management**: Rejected - error-prone, no versioning
- **JPA schema generation**: Rejected - not production-ready; loses control over schema

**Implementation Details**:
- Migration scripts in `src/main/resources/db/migration/`
- Naming convention: `V###__description.sql` (e.g., `V001__create_users_table.sql`)
- Executed in version order on application startup
- Uses plain SQL for maximum compatibility
- Migrations create tables, indexes, and constraints

---

### Authentication: JWT (JSON Web Tokens)

**Decision**: Use JWT bearer tokens with jjwt library (version 0.12.6)

**Rationale**:
- Spec requires "JWT bearer token authentication"
- Stateless authentication (no server-side session storage)
- Standard bearer token format (`Authorization: Bearer <token>`)
- Self-contained: token includes user ID, roles, and expiration
- Widely supported by clients and tools
- jjwt 0.12.6 is latest stable version (Java 17 compatible)
- Easy to validate token signature without database lookup (supports performance budget)

**Alternatives Considered**:
- **Spring Session with Redis**: Rejected - adds external dependency; spec requires JWT
- **Opaque tokens with database lookup**: Rejected - requires database hit on every request; slower
- **OAuth2**: Rejected - out of scope per spec

**Implementation Details**:
- Use jjwt for token generation and parsing
- Configure Spring Security filter chain for JWT validation
- Extract user ID from JWT claims for authentication
- Set reasonable token expiration (e.g., 24 hours)
- Use HMAC SHA-256 for token signing (sufficient for single-service architecture)

---

### Security: Spring Security 6.x

**Decision**: Use Spring Security 6.x (bundled with Spring Boot 3.5.9)

**Rationale**:
- Constitution Principle 5 requires Spring Security annotations (@PreAuthorize, @Secured, etc.)
- Built-in JWT support via filters and authentication managers
- Comprehensive authentication and authorization framework
- Works seamlessly with Spring Boot auto-configuration
- Supports method-level security if needed in future
- Latest version (6.x) aligns with Spring Boot 3.x

**Alternatives Considered**:
- **Custom security implementation**: Rejected - constitution requires Spring Security
- **Apache Shiro**: Rejected - not Spring-native; constitution requires Spring-First
- **JWT-only manual implementation**: Rejected - constitution requires Spring Security

**Implementation Details**:
- Configure `SecurityFilterChain` bean (not deprecated WebSecurityConfigurerAdapter)
- Use `JwtAuthenticationFilter` for Bearer token validation
- Configure public endpoints (/ping, /login) to permit all
- Configure protected endpoints (/users/*) to require authentication
- Use `@ControllerAdvice` to handle authentication exceptions (401 errors)

---

### Validation: Bean Validation (Jakarta Validation)

**Decision**: Use Jakarta Bean Validation 3.x with Spring integration

**Rationale**:
- Constitution Principle 5 requires Bean Validation annotations (@NotNull, @NotBlank, @Email, etc.)
- Standard Java validation approach
- Spring Boot includes validation starter
- Automatic validation via @Valid/@Validated annotations
- Consistent error messages for validation failures
- Supports custom validators via ConstraintValidator

**Alternatives Considered**:
- **Custom validation logic**: Rejected - constitution requires Bean Validation
- **Manual validation in controllers**: Rejected - boilerplate; Spring provides declarative validation

**Implementation Details**:
- Use `@Valid` on controller method parameters
- Use Bean Validation annotations on request DTOs
- Implement `@ControllerAdvice` to convert validation errors to standard error response
- Map MethodArgumentNotValidException to 400 with VALIDATION_FAILED error code

---

### API Contract: OpenAPI 3.0.1 with OpenAPI Generator

**Decision**: Use OpenAPI 3.0.1 specification with OpenAPI Generator Gradle plugin (version 7.10.0)

**Rationale**:
- Constitution Principle 2: "OpenAPI-First Contract + Generated Java Implementation"
- OpenAPI document is the SOURCE OF TRUTH for the public REST API
- Generated code MUST NOT be manually edited
- OpenAPI 3.0.1 is stable and widely supported
- OpenAPI Generator 7.10.0 supports Spring Boot 3.x code generation
- Gradle plugin integrates generation into build process

**Alternatives Considered**:
- **Code-first with Springdoc**: Rejected - constitution requires OpenAPI-first approach
- **Manual API implementation**: Rejected - violates constitution principle
- **Swagger Codegen**: Rejected - OpenAPI Generator is the successor with better Spring Boot 3 support

**Implementation Details**:
- OpenAPI spec location: `src/main/resources/openapi/user-management-api.yaml`
- Configure OpenAPI Generator Gradle plugin to generate:
  - API interfaces (Spring @RestController stubs)
  - Request/response model classes (POJOs with validation annotations)
- Generated code directory: `build/generated/openapi/`
- Add generated source directory to build path
- Implement generated API interfaces in controller classes

---

### Error Handling: Centralized via @ControllerAdvice

**Decision**: Use Spring @ControllerAdvice with @ExceptionHandler methods

**Rationale**:
- Constitution Principle 5 requires @ControllerAdvice for centralized error handling
- Constitution Principle 4 requires stable error codes and HTTP status-based retry semantics
- Single location for all error response formatting
- Consistent error response structure across all endpoints
- Handles all exception types (validation, not found, authentication, internal errors)
- Supports mapping exceptions to correct HTTP status codes

**Alternatives Considered**:
- **Per-controller exception handling**: Rejected - violates DRY; hard to maintain consistency
- **Manual try-catch in controllers**: Rejected - boilerplate; constitution requires @ControllerAdvice

**Implementation Details**:
- Create `GlobalExceptionHandler` class annotated with @ControllerAdvice
- Define @ExceptionHandler methods for each exception type
- Map exceptions to ErrorResponse with stable error codes:
  - MethodArgumentNotValidException → 400 VALIDATION_FAILED
  - ResourceNotFoundException → 404 RESOURCE_NOT_FOUND
  - DuplicateEmailException → 409 CONFLICT
  - AuthenticationException → 401 AUTHENTICATION_FAILED
  - ServiceUnavailableException → 503 SERVICE_UNAVAILABLE
  - Exception → 500 INTERNAL_ERROR
- Never include `retryable` field in error responses (constitution requirement)

---

### Feature Flags: Spring Configuration Properties

**Decision**: Use Spring @Value or @ConfigurationProperties for feature flags

**Rationale**:
- Constitution Principle 8 requires feature flags as Spring boolean properties
- Pattern: `FeatureFlag.<featureName>` (e.g., `FeatureFlag.usersApi`)
- Configurable via application.properties or environment variables
- No external dependencies needed
- Spring handles property injection automatically

**Alternatives Considered**:
- **External feature flag service**: Rejected - adds complexity; spec doesn't require it
- **Custom configuration system**: Rejected - Spring properties are sufficient

**Implementation Details**:
- Define property: `FeatureFlag.usersApi=false` in application.properties
- Inject via `@Value("${FeatureFlag.usersApi}")` in controllers or filter
- Create a filter or interceptor to check feature flag before processing requests
- Return 404 with standard error response when feature is disabled
- Don't reveal feature existence in error message

---

### Testing Framework: JUnit 5 + Spring Boot Test + Mockito

**Decision**: Use JUnit 5, Spring Boot Test, and Mockito for testing

**Rationale**:
- Constitution Principle 1 requires comprehensive test coverage
- JUnit 5 is the modern standard for Java testing
- Spring Boot Test provides full integration testing support
- Mockito is standard for mocking in Java
- Constitution requires tests for happy paths AND negative scenarios

**Alternatives Considered**:
- **JUnit 4**: Rejected - outdated; JUnit 5 is standard
- **TestNG**: Rejected - less common in Spring Boot ecosystem

**Implementation Details**:
- Unit tests: Test service layer logic with Mockito mocks
- Integration tests: Use @SpringBootTest with full application context
- Controller tests: Use @WebMvcTest with MockMvc
- Test coverage includes:
  - Happy paths (all endpoints)
  - Validation failures (400 errors)
  - Not found scenarios (404 errors)
  - Authentication failures (401 errors)
  - Conflict scenarios (409 errors)
  - Feature flag disabled (404 errors)
  - Transient failures (503 errors)

---

### Pagination Strategy: Offset-Based with Mandatory Parameters

**Decision**: Use offset-based pagination (page number + page size)

**Rationale**:
- Constitution Principle 7 requires pagination for all collection endpoints
- Simple to implement with Spring Data JDBC
- Familiar to most API consumers
- Supports random access to pages
- Aligns with common REST API patterns

**Alternatives Considered**:
- **Cursor-based pagination**: Rejected - more complex; not required for this use case
- **Limit-offset only**: Rejected - page-based is more intuitive for users

**Implementation Details**:
- Required query parameters: `page` (1-based, min 1) and `pageSize` (min 1, max 100)
- Response includes pagination metadata: `totalCount`, `page`, `pageSize`, `totalPages`
- Use Spring Data JDBC's `Pageable` and `Page` interfaces
- Validate pagination parameters (return 400 for invalid values)
- Define maximum page size (100) to prevent unbounded queries

---

## Architecture Decisions

### Layered Architecture

**Decision**: Use traditional layered architecture (Controller → Service → Repository)

**Rationale**:
- Clear separation of concerns
- Aligns with Spring Boot conventions
- Easy to understand and maintain
- Supports unit testing with mocks at each layer
- Appropriate for monolithic application

**Implementation Details**:
- **Controller Layer**: Handles HTTP requests, implements OpenAPI-generated interfaces
- **Service Layer**: Business logic, validation, orchestration
- **Repository Layer**: Data access via Spring Data JDBC
- **Model Layer**: Entities (database) and DTOs (API contracts from OpenAPI)

---

### Password Security

**Decision**: Use BCrypt password hashing with Spring Security's PasswordEncoder

**Rationale**:
- Industry standard for password hashing
- Spring Security provides built-in support
- Adaptive cost factor (resistance to brute force)
- Never store or return plaintext passwords

**Implementation Details**:
- Use `BCryptPasswordEncoder` bean
- Hash passwords before saving to database
- Compare passwords using encoder.matches() method
- Password field is write-only (never returned in responses)

---

### Bootstrap Mode for First User

**Decision**: Allow first user creation without authentication when database is empty

**Rationale**:
- Spec requirement: "The system allows creating the first user without authentication, but requires authentication for all user operations once at least one user exists"
- Enables system initialization without chicken-and-egg problem
- After first user exists, all operations require authentication

**Implementation Details**:
- In UserService.createUser(), check if database is empty
- If empty, allow creation without authentication check
- If not empty, enforce authentication requirement
- Document this behavior in quickstart guide

---

## Summary of Resolved Unknowns

All technical unknowns from the spec have been resolved:

1. **Language/Version**: Java 17 (explicitly specified in prompt)
2. **Framework**: Spring Boot 3.5.9 (explicitly specified in prompt)
3. **Build Tool**: Gradle (required by constitution, present in repository)
4. **Database**: H2 in-memory with PostgreSQL mode (suitable for demo/testing)
5. **Data Access**: Spring Data JDBC (aligns with constitution and spec requirements)
6. **Security**: Spring Security 6.x with JWT (spec requirement)
7. **Validation**: Jakarta Bean Validation (required by constitution)
8. **API Contract**: OpenAPI 3.0.1 with OpenAPI Generator (required by constitution)
9. **Error Handling**: @ControllerAdvice (required by constitution)
10. **Feature Flags**: Spring properties (required by constitution)
11. **Testing**: JUnit 5 + Spring Boot Test + Mockito (required by constitution)
12. **Pagination**: Offset-based (page/pageSize) (required by constitution)
13. **Password Hashing**: BCrypt (industry standard, Spring Security integration)
14. **Database Migrations**: Flyway (Spring Boot integration, SQL-based)

All decisions align with:
- Feature specification requirements
- Constitution principles (especially Spring-First Development)
- OpenAPI-first contract approach
- Performance budget (≤1s for synchronous operations)
- Error handling requirements (stable codes, HTTP-based retry semantics)
- Feature flag requirements
- Test coverage requirements
