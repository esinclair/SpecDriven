# Research: User Management API System

**Feature**: User Management API System | **Date**: 2025-12-29 | **Spec**: [spec.md](./spec.md)

## Overview

This document captures the technical research and decision-making process for implementing the User Management API System. All decisions are based on the requirements in spec.md and align with the SpecDriven constitution principles.

## Technology Stack Decisions

### Primary Framework: Spring Boot 3.5.9

**Decision**: Use Spring Boot 3.5.9 (latest stable 3.5.x series)

**Rationale**:
- Aligns with specified constraint: "Use Spring Boot 3.5.x (latest)"
- Spring Boot 3.x series uses Jakarta EE (jakarta.* packages) required for modern Java
- Version 3.5.9 is the latest stable release as of December 2025
- Built-in support for OpenAPI integration via annotations and code generation
- Comprehensive testing support with Spring Boot Test
- Mature ecosystem for REST API development

**Alternatives Considered**:
- **Spring Boot 2.x**: Rejected - older javax.* namespace; not requested version
- **Micronaut**: Rejected - would require learning new framework; Spring Boot specified
- **Quarkus**: Rejected - would require different build/runtime model; Spring Boot specified

**Implementation Details**:
- Use Spring Initializr conventions for project structure
- Leverage Spring Boot starters: web, data-jdbc, security, validation
- Use Spring Boot auto-configuration for database, security, and web layers

---

### Java Version: JDK 17

**Decision**: Use Java 17 (LTS)

**Rationale**:
- Aligns with specified constraint: "Use JDK 17"
- Java 17 is a Long-Term Support (LTS) release
- Required for Spring Boot 3.5.x (minimum Java 17)
- Provides modern language features: records, pattern matching, sealed classes
- Wide tooling and IDE support
- Production-ready and stable

**Alternatives Considered**:
- **Java 21 (LTS)**: Rejected - requirement specifies JDK 17
- **Java 11 (LTS)**: Rejected - incompatible with Spring Boot 3.x
- **Java 8**: Rejected - too old; not compatible with Spring Boot 3.x

**Implementation Details**:
- Configure Gradle toolchain to use Java 17: `java.toolchain.languageVersion = JavaLanguageVersion.of(17)`
- Use modern Java features where appropriate (text blocks for SQL, records for immutable data)

---

### Database: H2 In-Memory (SQL-Compliant Mode)

**Decision**: Use H2 in-memory database in PostgreSQL compatibility mode

**Rationale**:
- Aligns with specified constraint: "Use H2 in-memory database in SQL compliant manner"
- Zero external dependencies for development and testing
- Fast startup and teardown for tests
- PostgreSQL mode ensures SQL standards compliance (no H2-specific syntax)
- Supports migrations that can work with production databases (PostgreSQL, MySQL)
- Embedded mode suitable for demo and testing purposes

**Alternatives Considered**:
- **PostgreSQL**: Rejected - requires external database server; spec specifies H2
- **MySQL**: Rejected - requires external database server; spec specifies H2
- **SQLite**: Rejected - spec specifies H2
- **H2 default mode**: Rejected - uses non-standard SQL; spec requires SQL-compliant

**Implementation Details**:
- Connection URL: `jdbc:h2:mem:specdriven;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`
- MODE=PostgreSQL enables SQL standards compliance
- DB_CLOSE_DELAY=-1 keeps database alive until JVM shutdown
- Enable H2 console for debugging: `/h2-console`

---

### Data Access: Spring Data JDBC

**Decision**: Use Spring Data JDBC repositories

**Rationale**:
- Aligns with specified constraint: "Implement database access as Spring Repositories"
- Simpler than JPA: no lazy loading, no session management, no proxy objects
- Explicit control over SQL queries
- Better performance for simple CRUD operations
- No N+1 query problems (common with JPA)
- Lightweight and fits the application's needs (no complex object graphs)

**Alternatives Considered**:
- **Spring Data JPA**: Rejected - adds unnecessary complexity for simple CRUD operations
- **JDBC Template**: Rejected - more boilerplate; spec asks for "Spring Repositories"
- **MyBatis**: Rejected - spec specifies Spring Repositories

**Implementation Details**:
- Repositories extend `CrudRepository<Entity, ID>`
- Use `@Query` annotation for custom queries (filtering, pagination)
- Entity classes use `@Table`, `@Id`, and `@Column` annotations
- No lazy loading or complex relationships

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

**Alternatives Considered**:
- **Liquibase**: Rejected - XML/YAML config adds complexity; Flyway is simpler
- **Manual schema management**: Rejected - error-prone, no versioning
- **JPA schema generation**: Rejected - not production-ready; loses control over schema

**Implementation Details**:
- Migration scripts in `src/main/resources/db/migration/`
- Naming convention: `V###__description.sql` (e.g., `V001__init_schema.sql`)
- Executed in version order on application startup
- Uses plain SQL for maximum compatibility

---

### Authentication: JWT (JSON Web Tokens)

**Decision**: Use JWT bearer tokens with jjwt library (version 0.12.3)

**Rationale**:
- Stateless authentication (no server-side session storage)
- Standard bearer token format (`Authorization: Bearer <token>`)
- Self-contained: token includes user ID and expiration
- Widely supported by clients and tools
- jjwt is mature and well-maintained library
- Easy to validate token signature without database lookup

**Alternatives Considered**:
- **Spring Session with Redis**: Rejected - adds external dependency; spec implies stateless JWT
- **Opaque tokens with database lookup**: Rejected - requires database hit on every request; slower
- **OAuth2**: Rejected - out of scope per spec; adds complexity

**Implementation Details**:
- Library: `io.jsonwebtoken:jjwt-api:0.12.3` with runtime implementations
- Token structure: Header.Payload.Signature (standard JWT format)
- Payload claims: `sub` (user ID), `exp` (expiration), `iat` (issued at)
- Signing algorithm: HS256 (HMAC with SHA-256)
- Secret key: configurable via `jwt.secret` property (environment variable in production)
- Expiration: configurable via `jwt.expirationMs` property (default 24 hours)

---

### Password Hashing: BCrypt

**Decision**: Use BCrypt for password hashing (via Spring Security)

**Rationale**:
- Industry standard for password storage
- Adaptive hashing: automatically adjusts cost factor
- Salted hashes (each password has unique salt)
- Built into Spring Security as `BCryptPasswordEncoder`
- Resistant to rainbow table and brute force attacks
- Configurable work factor for future-proofing

**Alternatives Considered**:
- **Plain text**: Rejected - completely insecure
- **SHA-256**: Rejected - too fast; vulnerable to brute force
- **Argon2**: Rejected - BCrypt is standard in Spring Security; simpler integration

**Implementation Details**:
- Use `BCryptPasswordEncoder` from Spring Security
- Hash passwords before storing in database
- Verify passwords by comparing hashes
- Never store or return plain text passwords

---

### API Contract: OpenAPI 3.0 with Code Generation

**Decision**: Use OpenAPI 3.0.3 specification with OpenAPI Generator Gradle plugin

**Rationale**:
- Aligns with constitution principle: "OpenAPI-First Contract + Generated Java Implementation"
- OpenAPI is the source of truth for the API
- Code generation ensures contract and implementation stay in sync
- Generated interfaces prevent manual drift
- Industry standard for REST API documentation
- Tooling support for validation and testing

**Alternatives Considered**:
- **Manual API definition**: Rejected - violates constitution; allows contract drift
- **Swagger 2.0**: Rejected - older spec; OpenAPI 3.0 is current standard
- **GraphQL**: Rejected - spec describes REST API, not GraphQL

**Implementation Details**:
- Specification file: `src/main/resources/openapi.yaml`
- Generator: OpenAPI Generator Gradle plugin (`org.openapi.generator:7.14.0`)
- Generator mode: `spring` with `interfaceOnly: true`
- Generated output: `src/main-gen/java/` (separate source root)
- Regenerate on every build before compilation
- Controllers implement generated interfaces

---

### Feature Flags: Spring Configuration Properties

**Decision**: Use Spring `@ConfigurationProperties` for boolean feature flags

**Rationale**:
- Aligns with constitution principle: "Feature Flags (Spring Properties)"
- Native Spring Boot support, no external libraries
- Type-safe configuration with validation
- Can be overridden via application.yml, environment variables, or command-line args
- Simple boolean flags (true = enabled, false = disabled)
- Naming convention: `FeatureFlag.<featureName>`

**Alternatives Considered**:
- **LaunchDarkly / Togglz**: Rejected - adds external dependency; overkill for simple flags
- **Database-backed flags**: Rejected - adds complexity; config properties are sufficient
- **Environment variables only**: Rejected - less structured; harder to validate

**Implementation Details**:
- Configuration class: `@ConfigurationProperties("FeatureFlag")`
- Flag naming: `FeatureFlag.usersApi: false` in application.yml
- Custom security filter checks flag before allowing request
- Default value: `false` (disabled) until feature validated
- Tests override flag value in test configuration

---

### Testing Framework: JUnit 5 + Spring Boot Test

**Decision**: Use JUnit 5 (Jupiter) with Spring Boot Test support

**Rationale**:
- JUnit 5 is the modern standard for Java testing
- Spring Boot Test provides excellent integration testing support
- `@SpringBootTest` loads full application context for realistic tests
- `@MockMvc` for controller testing without starting HTTP server
- Parameterized tests and test lifecycle improvements over JUnit 4
- Aligns with specified constraint: "All test cases must pass"

**Alternatives Considered**:
- **JUnit 4**: Rejected - older version; JUnit 5 is current standard
- **TestNG**: Rejected - JUnit 5 is Spring Boot default and widely adopted
- **Spock (Groovy)**: Rejected - adds Groovy dependency; JUnit 5 is sufficient

**Implementation Details**:
- Unit tests: Plain JUnit 5 with Mockito for mocking
- Integration tests: `@SpringBootTest` + `@AutoConfigureMockMvc`
- Test database: H2 in-memory, reset between tests
- Assertions: JUnit 5 assertions + Spring's `MockMvc` matchers

---

### Mocking Framework: Mockito

**Decision**: Use Mockito for test mocking

**Rationale**:
- Industry standard mocking framework for Java
- First-class Spring Boot integration
- Clean and readable syntax
- Supports all common mocking patterns (stubs, verification, argument capture)
- Wide adoption means good documentation and examples

**Alternatives Considered**:
- **EasyMock**: Rejected - less popular, less intuitive syntax
- **PowerMock**: Rejected - too invasive; avoid mocking static/private methods
- **No mocking**: Rejected - integration tests alone would be too slow

**Implementation Details**:
- Use `@Mock` and `@InjectMocks` for unit tests
- Mock repositories and external dependencies in service tests
- Verify interactions with `verify(mock).method()`
- Integration tests use real beans (no mocking)

---

### Build Tool: Gradle with Wrapper

**Decision**: Use Gradle 8.x with Gradle Wrapper

**Rationale**:
- Aligns with constraint: Project already uses Gradle (build.gradle exists)
- Constitution specifies: "Use Gradle as the single source of build truth"
- Gradle Wrapper ensures consistent builds across environments
- Native support for OpenAPI Generator plugin
- Better performance than Maven for large projects
- Groovy/Kotlin DSL for build scripts

**Alternatives Considered**:
- **Maven**: Rejected - project already uses Gradle
- **Bazel**: Rejected - overkill for single Spring Boot application

**Implementation Details**:
- Use Gradle Wrapper: `./gradlew` (Linux/Mac) or `gradlew.bat` (Windows)
- Build command: `./gradlew clean build`
- OpenAPI generation task: `./gradlew openApiGenerate`
- Run application: `./gradlew bootRun`

---

## Architecture Patterns Research

### Layered Architecture

**Decision**: Use classic layered architecture (Controller → Service → Repository)

**Rationale**:
- Clear separation of concerns
- Standard Spring Boot pattern
- Easy to test each layer independently
- Matches Spring's component model (@Controller, @Service, @Repository)
- Appropriate for CRUD-heavy applications

**Layer Responsibilities**:
1. **Controller**: Handle HTTP, delegate to service
2. **Service**: Business logic, validation, transactions
3. **Repository**: Data access only
4. **Domain**: Entity classes (database models)
5. **API Models**: DTOs (generated from OpenAPI)

---

### Error Handling Strategy

**Decision**: Use `@ControllerAdvice` with exception-to-HTTP-status mapping

**Rationale**:
- Aligns with constitution: "Error Codes + HTTP Status Indicate Retryability"
- Centralized error handling (DRY principle)
- Consistent error response format across all endpoints
- Maps exceptions to stable error codes
- HTTP status indicates retry behavior (4xx = don't retry, 5xx = may retry)

**Error Response Structure**:
```json
{
  "code": "VALIDATION_FAILED",
  "message": "Email address is required"
}
```

**Exception Mapping**:
- `ValidationException` → 400 + `VALIDATION_FAILED`
- `ResourceNotFoundException` → 404 + `RESOURCE_NOT_FOUND`
- `ConflictException` → 409 + `CONFLICT`
- `AuthenticationException` → 401 + `AUTHENTICATION_FAILED`
- `DataAccessException` → 503 + `SERVICE_UNAVAILABLE`
- `Exception` (catch-all) → 500 + `INTERNAL_ERROR`

---

### Pagination Strategy

**Decision**: Use limit/offset pagination with mandatory parameters

**Rationale**:
- Aligns with constitution: "Paged Result Sets for Collections"
- Simple to implement with SQL LIMIT/OFFSET
- Predictable performance (no unbounded queries)
- Mandatory parameters prevent accidental full scans
- Page metadata helps clients navigate results

**Pagination Parameters**:
- `page`: 1-based page number (minimum: 1, required)
- `pageSize`: Items per page (minimum: 1, maximum: 100, required)

**Response Metadata**:
```json
{
  "items": [...],
  "page": 1,
  "pageSize": 10,
  "totalCount": 100,
  "totalPages": 10
}
```

---

### Security Filter Chain

**Decision**: Custom filter chain with JWT validation and feature flag enforcement

**Rationale**:
- Spring Security provides filter chain framework
- Custom filters for JWT validation and feature flag checks
- Order matters: feature flag check → JWT validation → endpoint
- Health check bypasses all security checks

**Filter Order**:
1. `FeatureFlagSecurityFilter` (checks if feature enabled)
2. `JwtAuthenticationFilter` (validates bearer token)
3. Spring Security authorization
4. Controller method execution

---

## Performance Considerations

### Database Indexing

**Decision**: Index frequently queried columns

**Rationale**:
- Email lookups for login and uniqueness checks
- Username filtering in list operations
- User ID (primary key) for all CRUD operations
- Role filtering in list operations

**Indexes**:
- Primary key on `users.id` (automatic)
- Unique index on `users.email_address`
- Non-unique index on `users.username`
- Index on `user_roles.user_id`
- Index on `user_roles.role_id`

---

### Query Optimization

**Decision**: Use Spring Data JDBC projections and custom queries

**Rationale**:
- Avoid N+1 queries by fetching roles with users in single query
- Use JOIN when loading user with roles
- Paginate at database level with LIMIT/OFFSET
- Filter at database level (not in Java code)

**Example Query**:
```sql
SELECT u.*, r.role_name 
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.email_address = :email
LIMIT :limit OFFSET :offset
```

---

## Security Best Practices

### Password Security

**Research Findings**:
- Never store passwords in plain text
- Use adaptive hashing (BCrypt, Argon2, PBKDF2)
- Salt each password uniquely (automatic with BCrypt)
- Never log or return passwords in responses
- Validate password strength at API boundary

**Implementation**:
- Hash passwords before storing: `BCryptPasswordEncoder.encode(plainPassword)`
- Validate passwords: `BCryptPasswordEncoder.matches(plainPassword, hashedPassword)`
- Password field write-only in API (not in response DTOs)

---

### JWT Security

**Research Findings**:
- Use strong secret key (minimum 256 bits for HS256)
- Rotate secret keys periodically
- Set reasonable expiration times (24 hours typical)
- Validate signature and expiration on every request
- Never store sensitive data in JWT payload (it's base64, not encrypted)

**Implementation**:
- Secret from environment variable: `${JWT_SECRET}`
- Default development secret documented as insecure
- Production deployment must override secret
- Token expiration: 24 hours (configurable)
- No PII in token payload (only user ID)

---

### Non-Enumeration

**Research Findings**:
- Login errors should not reveal if username exists
- Return same error for unknown username and wrong password
- Prevents username enumeration attacks

**Implementation**:
- Return `AUTHENTICATION_FAILED` for both cases
- Same HTTP status (401) for both cases
- Same error message for both cases
- Timing attacks mitigated by consistent password hashing (always hash, even if user not found)

---

## Testing Strategy Research

### Test Pyramid

**Decision**: Follow test pyramid with unit tests at base, integration tests at top

**Rationale**:
- Unit tests are fast and cheap (no Spring context)
- Integration tests are slower but validate full stack
- Aim for 70% unit tests, 30% integration tests
- Both are required per constitution

**Test Types**:
1. **Unit Tests**: Service logic, mappers, utilities (fast, isolated)
2. **Integration Tests**: Full HTTP request-response (slower, realistic)
3. **Performance Tests**: Validate ≤1s budget (smoke tests, not exhaustive)

---

### Negative Testing

**Research Findings**:
- Constitution requires negative tests for all endpoints
- Test validation failures, not found, conflicts, auth failures
- Verify error response structure and stable codes
- Test retry behavior (4xx vs 5xx)

**Coverage**:
- Every endpoint must have negative test for invalid input
- Every protected endpoint must test 401 unauthorized
- Every endpoint that can return 404 must test not found
- Every endpoint with unique constraints must test 409 conflict

---

## Decision Summary

| Category | Technology | Rationale |
|----------|-----------|-----------|
| Framework | Spring Boot 3.5.9 | Specified requirement; mature REST API framework |
| Language | Java 17 (JDK 17) | Specified requirement; Spring Boot 3.x minimum |
| Database | H2 in-memory (PostgreSQL mode) | Specified requirement; zero setup for dev/test |
| Data Access | Spring Data JDBC | Specified requirement (Spring Repositories); simpler than JPA |
| Migrations | Flyway | Industry standard; SQL-based; Spring Boot native support |
| Authentication | JWT (jjwt 0.12.3) | Stateless; standard bearer tokens; self-contained |
| Password Hashing | BCrypt (Spring Security) | Adaptive; salted; Spring Security built-in |
| API Contract | OpenAPI 3.0.3 + Generator | Constitution requirement; contract-first development |
| Feature Flags | Spring ConfigurationProperties | Constitution requirement; native Spring support |
| Testing | JUnit 5 + Spring Boot Test | Modern standard; excellent Spring integration |
| Mocking | Mockito | Industry standard; clean syntax; Spring integration |
| Build Tool | Gradle 8.x with Wrapper | Existing project setup; constitution requirement |

---

## Open Questions & Clarifications

### ✅ Resolved

- **Q**: Which Spring Boot version exactly?  
  **A**: 3.5.9 (latest 3.5.x as of Dec 2025)

- **Q**: Which database for production?  
  **A**: H2 for development/testing per spec; production can use PostgreSQL/MySQL (schema compatible)

- **Q**: How to handle concurrent user updates?  
  **A**: Last write wins (no optimistic locking required by spec)

- **Q**: Password complexity requirements?  
  **A**: Out of scope per spec; accept any non-empty password

- **Q**: Token refresh mechanism?  
  **A**: Out of scope; tokens expire, client must re-login

### ⚠️ Assumptions (Document for Stakeholders)

- JWT secret must be externalized in production (not use default)
- H2 in-memory database acceptable for demo/testing; production needs persistent DB
- Single-tenant application (no multi-tenancy isolation)
- No rate limiting in initial version
- No authorization beyond authentication (all authenticated users can do anything)
- Email verification not required (users immediately active)

---

**Research Status**: ✅ Complete  
**Next Phase**: Phase 1 - Design & Contracts  
**Last Updated**: 2025-12-29

