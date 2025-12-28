# Research: SpecDriven API System

**Feature**: Overall System Architecture  
**Branch**: `main`  
**Date**: 2025-12-27  
**Status**: Complete

## Purpose

This document captures research findings and technical decisions for the SpecDriven API system architecture. All technical context items have been verified against the existing codebase, and no clarifications were needed as the system is already implemented.

## Research Areas

### 1. OpenAPI Code Generation with Spring Boot

**Decision**: Use OpenAPI Generator 7.14.0 with Spring generator

**Rationale**:
- Official OpenAPI Generator plugin provides mature Spring Boot 3 support
- Generates type-safe interfaces and DTOs from OpenAPI specification
- Supports Jakarta EE (required for Spring Boot 3.x)
- `interfaceOnly=true` mode generates controller interfaces that handwritten code implements
- Keeps generated and handwritten code separate (`src/main-gen` vs `src/main`)

**Implementation Details**:
```groovy
- Generator: 'spring'
- Config: interfaceOnly=true, useSpringBoot3=true, useJakartaEe=true
- Output: src/main-gen/java (never manually edited)
- Packages: 
  - API interfaces: com.example.specdriven.api
  - Models: com.example.specdriven.api.model
  - Invoker: com.example.specdriven.api.invoker
```

**Alternatives Considered**:
- Manual coding: Rejected due to risk of contract drift
- Swagger Codegen: Rejected; OpenAPI Generator is the maintained fork
- Other generators (TypeScript, Go): Not applicable for Java Spring Boot

**References**:
- OpenAPI Generator Spring documentation
- Existing build.gradle configuration
- Spring Boot 3 migration guide

---

### 2. JWT Authentication with Spring Security

**Decision**: Use io.jsonwebtoken (jjwt) 0.12.3 with Spring Security

**Rationale**:
- jjwt is the most widely used JWT library for Java
- Version 0.12.3 provides modern API and security fixes
- Spring Security 6.x (included in Spring Boot 3.5.x) provides excellent JWT support
- Stateless authentication scales well
- Bearer token pattern is industry standard

**Implementation Details**:
```java
- Token generation: JwtService creates tokens on successful login
- Token validation: JwtAuthenticationFilter validates bearer tokens
- Security configuration: SecurityConfig defines protected endpoints
- Bootstrap mode: Special handling for POST /users when user count = 0
```

**Configuration**:
```properties
jwt.secret=<configurable, must be >=256 bits for HS256>
jwt.expiration-ms=3600000 (1 hour default)
```

**Alternatives Considered**:
- Spring Session: Rejected; stateful sessions don't scale as well
- OAuth2/OIDC: Deferred; adds complexity for this use case
- Basic Auth: Rejected; tokens provide better security and UX

**Security Considerations**:
- Secret must be changed in production (documented in quickstart)
- Token expiration enforced
- Password fields excluded from JSON serialization
- Error messages don't reveal username existence

**References**:
- jjwt documentation: https://github.com/jwtk/jjwt
- Spring Security JWT guide
- OWASP authentication best practices

---

### 3. Database Schema Management with Flyway

**Decision**: Use Flyway for all database migrations

**Rationale**:
- Flyway is the industry standard for database version control
- Integrates seamlessly with Spring Boot
- Version-based migration files (V001__, V002__, etc.) provide clear history
- Supports rollback strategies
- Works with multiple database types

**Implementation Details**:
```
- Migration location: src/main/resources/db/migration/
- Naming: V{number}__{description}.sql
- Execution: Automatic on Spring Boot startup
- Configuration: 
  - spring.flyway.enabled=true
  - spring.flyway.baseline-on-migrate=true (allows starting with existing DB)
```

**Existing Migrations**:
- V003__users_api.sql: Creates users table, roles, and relationships

**Alternatives Considered**:
- Liquibase: Similar capabilities; Flyway chosen for simplicity
- JPA/Hibernate auto-DDL: Rejected; not suitable for production
- Manual SQL scripts: Rejected; no version tracking or automation

**Best Practices**:
- Never modify existing migrations (create new ones)
- Test migrations on copy of production data
- Keep migrations idempotent where possible
- Include both schema and reference data

**References**:
- Flyway documentation: https://flywaydb.org/documentation/
- Spring Boot Flyway integration
- Database migration best practices

---

### 4. Feature Flag Implementation with Spring Properties

**Decision**: Use Spring @ConfigurationProperties for feature flags

**Rationale**:
- Native Spring Boot mechanism, no external dependencies
- Type-safe boolean properties
- Externalized configuration (can override via environment variables)
- Simple @ConditionalOnProperty annotations for bean activation
- Easy to test both enabled/disabled states

**Implementation Pattern**:
```properties
# application.properties
feature-flag.users-api=false

# Java
@ConditionalOnProperty(name = "feature-flag.users-api", havingValue = "true")
public class UsersFeatureConfig { ... }
```

**Naming Convention**: `feature-flag.<featureName>` (lowercase with hyphens)
- Maps to Spring property naming standards
- Clear prefix identifies feature flags
- Boolean values only (true/false)

**Runtime Behavior**:
- Flag disabled → endpoint returns 404 with standard error response
- Flag enabled → endpoint processes normally
- No flag → default behavior (typically disabled for new features)

**Alternatives Considered**:
- Togglz: Rejected; adds dependency and complexity for simple use case
- LaunchDarkly/Split: Rejected; external service not needed
- Custom solution: Rejected; Spring's built-in support is sufficient
- Environment variables: Supported as override mechanism

**Testing Strategy**:
- Use @TestPropertySource to test both flag states
- Verify 404 response when disabled
- Verify normal processing when enabled

**References**:
- Spring Boot externalized configuration
- @ConditionalOnProperty documentation

---

### 5. Error Response Standardization

**Decision**: Global exception handler with stable error codes

**Rationale**:
- Single point of control for all error responses
- Consistent shape across all endpoints (code + message)
- HTTP status codes indicate retryability per constitution
- Spring's @ControllerAdvice provides perfect mechanism

**Error Response Schema**:
```yaml
ErrorResponse:
  type: object
  required: [code, message]
  properties:
    code:
      type: string
      description: Stable error identifier (e.g., VALIDATION_FAILED)
    message:
      type: string
      description: Human-readable error description
```

**Standard Error Codes**:
- `VALIDATION_FAILED`: 400 - Invalid input data
- `RESOURCE_NOT_FOUND`: 404 - Requested resource doesn't exist
- `CONFLICT`: 409 - Resource already exists (duplicate)
- `UNAUTHORIZED`: 401 - Authentication required or failed
- `FORBIDDEN`: 403 - Authenticated but insufficient permissions
- `INTERNAL_ERROR`: 500 - Unexpected server error
- `SERVICE_UNAVAILABLE`: 503 - Temporary unavailability
- `FEATURE_DISABLED`: 404 - Feature flag is disabled

**HTTP Status Semantics**:
- 4xx: Client error, non-retryable (except 429 with Retry-After)
- 5xx: Server error, retryable
- Standard headers: Retry-After for 503/429

**Implementation**:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(...) {
    // Returns 400 with VALIDATION_FAILED
  }
  
  // Additional handlers for each error type
}
```

**Alternatives Considered**:
- Per-controller error handling: Rejected; inconsistent
- Including "retryable" field: Rejected per constitution
- Stack traces in responses: Rejected; security risk

**References**:
- Constitution principle #4: Error Codes + HTTP Status
- RFC 7231: HTTP semantics
- OWASP error handling guidelines

---

### 6. H2 Database for Development

**Decision**: H2 in-memory database with PostgreSQL compatibility mode

**Rationale**:
- Zero external dependencies for local development
- Fast test execution (in-memory)
- PostgreSQL mode ensures SQL compatibility
- Easy transition to real PostgreSQL for production
- Built into Spring Boot

**Configuration**:
```properties
spring.datasource.url=jdbc:h2:mem:specdriven;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
```

**Production Considerations**:
- Replace with PostgreSQL/MySQL/Oracle by changing datasource properties
- Flyway migrations are database-agnostic (use standard SQL)
- Spring Data JDBC abstracts database specifics
- Connection pooling configuration for production

**Alternatives Considered**:
- PostgreSQL via Docker: Rejected; adds setup complexity for developers
- Embedded PostgreSQL: Rejected; H2 is simpler and faster
- SQLite: Rejected; less SQL feature-complete than H2

**Testing Strategy**:
- Integration tests use same H2 configuration
- Each test gets clean database (Spring's @DirtiesContext)
- Flyway migrations run before tests

**References**:
- H2 Database documentation
- Spring Boot H2 integration
- PostgreSQL compatibility mode

---

### 7. Pagination Implementation

**Decision**: Required page and pageSize query parameters with metadata response

**Rationale**:
- Prevents unbounded result sets per constitution
- Standard pagination pattern
- Predictable performance
- Client can control page size within limits

**Implementation Pattern**:
```yaml
parameters:
  - name: page
    in: query
    required: true
    schema:
      type: integer
      minimum: 0
  - name: pageSize
    in: query
    required: true
    schema:
      type: integer
      minimum: 1
      maximum: 100

response:
  UserPage:
    properties:
      content: array of users
      page: current page number
      pageSize: items per page
      totalPages: total pages available
      totalElements: total matching items
```

**Validation**:
- page >= 0
- pageSize between 1 and 100
- Invalid values return 400

**Database Implementation**:
- Spring Data JDBC Pageable support
- SQL LIMIT and OFFSET
- COUNT query for totalElements

**Alternatives Considered**:
- Cursor-based pagination: Deferred; offset-based is simpler
- Optional pagination: Rejected per constitution (paging is required)
- Default page size: Rejected; clients must be explicit

**References**:
- Constitution principle #7: Paged Result Sets
- REST pagination best practices

---

### 8. Lombok for Boilerplate Reduction

**Decision**: Use Lombok annotations for generated DTOs and entities

**Rationale**:
- Reduces boilerplate (getters, setters, constructors, builders)
- OpenAPI Generator supports Lombok generation
- Improves code readability
- Type-safe builder pattern
- No runtime dependency (compile-time annotation processing)

**Recommended Annotations**:
```java
@Data                    // Getters, setters, toString, equals, hashCode
@Builder                 // Builder pattern
@NoArgsConstructor       // Default constructor (JPA requirement)
@AllArgsConstructor      // All-args constructor
```

**Configuration in OpenAPI Generator**:
- Currently not enabled in generator config
- Manual addition recommended for handwritten entities
- OpenAPI-generated models don't use Lombok (generator limitation)

**Note**: User specified Lombok should be used whenever possible, so this should be implemented for handwritten code (entities, service classes, etc.)

**Alternatives Considered**:
- Manual getters/setters: Verbose and error-prone
- Java Records: Not suitable for mutable entities
- IDE generation: Not version-controlled

**References**:
- Lombok documentation: https://projectlombok.org/
- Lombok with Spring Boot
- Lombok best practices

---

## Technology Stack Summary

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Framework | Spring Boot | 3.5.9 | Application framework |
| Language | Java | 17 | Programming language |
| Build Tool | Gradle | 8.x (wrapper) | Build automation |
| API Spec | OpenAPI | 3.0.3 | API contract definition |
| Code Gen | OpenAPI Generator | 7.14.0 | Generate interfaces/DTOs |
| Security | Spring Security | 6.x | Authentication/authorization |
| JWT | jjwt | 0.12.3 | Token generation/validation |
| Database | H2 | 2.x | Development database |
| Migration | Flyway | Latest | Schema version control |
| Testing | JUnit | 5 | Test framework |
| Testing | Spring Boot Test | 3.5.9 | Integration testing |
| Mocking | Mockito | Latest | Test mocking |

## Integration Points

### Build Process
1. OpenAPI Generator runs before compilation
2. Generated code placed in src/main-gen/java
3. Gradle compiles main + generated sources
4. Tests run with in-memory H2 database
5. Flyway migrations applied automatically

### Runtime Process
1. Spring Boot starts
2. Flyway runs migrations
3. Security filters initialize
4. Feature flags evaluated
5. Controllers registered (if feature enabled)
6. Application ready for requests

### Development Workflow
1. Update OpenAPI spec (src/main/resources/openapi.yaml)
2. Run `./gradlew clean build` to regenerate code
3. Implement/update controller implementations
4. Write/update tests
5. Verify with `./gradlew test`

## Unresolved Items

✅ **None**: All technical context items have been verified against the existing codebase. The system is already implemented and operational.

## Next Steps (Phase 1)

1. Generate data-model.md documenting entities and relationships
2. Generate contracts/openapi.contract.yaml (copy of current OpenAPI spec as reference)
3. Generate quickstart.md for developers
4. Update agent context with technology decisions

