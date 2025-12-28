# Data Model: SpecDriven API System

**Feature**: Overall System Architecture  
**Branch**: `main`  
**Date**: 2025-12-27  
**Status**: Complete

## Purpose

This document describes the data entities, relationships, validation rules, and state transitions for the SpecDriven API system.

## Entity Overview

The system manages the following core entities:
1. **User**: System users with authentication credentials
2. **Role**: Access control roles (enum-based)
3. **UserRole**: Many-to-many relationship between users and roles
4. **ErrorResponse**: Standard error response structure (not persisted)
5. **PingResponse**: Health check response (not persisted)

---

## Entity Definitions

### User

Represents a system user with authentication credentials and profile information.

**Table**: `users`

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, NOT NULL, Auto-generated | Unique identifier |
| username | String(50) | NOT NULL, UNIQUE | Login username |
| name | String(100) | NOT NULL | Display name |
| password | String(255) | NOT NULL | Hashed password (bcrypt) |
| emailAddress | String(255) | NOT NULL, UNIQUE | Email address |
| createdAt | Timestamp | NOT NULL, Default=now() | Creation timestamp |
| updatedAt | Timestamp | NOT NULL, Default=now() | Last update timestamp |

**Validation Rules**:
- `username`: 
  - Required
  - 3-50 characters
  - Alphanumeric and underscore only
  - Unique across system
- `name`:
  - Required
  - 1-100 characters
  - Any printable characters
- `password`:
  - Required on creation
  - Minimum 8 characters
  - Must be hashed before storage (bcrypt with strength 10)
  - Never returned in API responses
- `emailAddress`:
  - Required
  - Valid email format (RFC 5322)
  - Unique across system
  - Case-insensitive for uniqueness check

**Indexes**:
```sql
CREATE UNIQUE INDEX idx_users_username ON users(LOWER(username));
CREATE UNIQUE INDEX idx_users_email ON users(LOWER(email_address));
```

**Relationships**:
- One-to-Many with `user_roles` (a user can have multiple roles)

**Java Entity** (with Lombok):
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {
    @Id
    private UUID id;
    
    private String username;
    private String name;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;  // Never serialize in responses
    
    private String emailAddress;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Transient field populated from join
    @Transient
    private Set<RoleName> roles;
}
```

**State Transitions**:
- **Created**: User is created with hashed password
- **Updated**: Name, email, or password can be updated
- **Roles Modified**: Roles can be added or removed
- **Deleted**: User is removed from system (cascade deletes user_roles)

---

### Role (Enum)

Represents predefined access control roles. Implemented as an enum, not a database table.

**Enum**: `RoleName`

**Values**:

| Value | Description |
|-------|-------------|
| ADMIN | Full system access, can manage users and roles |
| USER | Standard user access |
| GUEST | Limited read-only access |

**Java Enum**:
```java
public enum RoleName {
    ADMIN("ADMIN"),
    USER("USER"),
    GUEST("GUEST");
    
    private final String value;
    
    RoleName(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
```

**Validation**:
- Must be one of the predefined values
- Case-sensitive
- Invalid values return 400 VALIDATION_FAILED

**Note**: Roles are predefined enums for simplicity. A future enhancement could make roles database-driven with custom permissions.

---

### UserRole (Join Table)

Represents the many-to-many relationship between users and roles.

**Table**: `user_roles`

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| userId | UUID | FK to users.id, NOT NULL | User reference |
| roleName | String(50) | NOT NULL | Role enum value |

**Composite Key**: (userId, roleName)

**Constraints**:
```sql
PRIMARY KEY (user_id, role_name)
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
CHECK (role_name IN ('ADMIN', 'USER', 'GUEST'))
```

**Indexes**:
```sql
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_name);
```

**Operations**:
- **Assign Role**: INSERT (idempotent - ignore if exists)
- **Remove Role**: DELETE (idempotent - ignore if not exists)
- **List User Roles**: SELECT by userId
- **List Users by Role**: SELECT by roleName (for filtering in GET /users)

---

## Non-Persisted Entities

### ErrorResponse

Standard error response returned for all 4xx and 5xx responses.

**Schema**:
```yaml
ErrorResponse:
  type: object
  required: [code, message]
  properties:
    code:
      type: string
      description: Stable error identifier
      example: VALIDATION_FAILED
    message:
      type: string
      description: Human-readable error description
      example: Invalid input data
```

**Error Codes**:

| Code | HTTP Status | Meaning | Retryable |
|------|-------------|---------|-----------|
| VALIDATION_FAILED | 400 | Invalid input data | No |
| UNAUTHORIZED | 401 | Authentication required or invalid | No |
| FORBIDDEN | 403 | Insufficient permissions | No |
| RESOURCE_NOT_FOUND | 404 | Requested resource doesn't exist | No |
| FEATURE_DISABLED | 404 | Feature flag is disabled | No |
| CONFLICT | 409 | Resource already exists | No |
| INTERNAL_ERROR | 500 | Unexpected server error | Yes |
| SERVICE_UNAVAILABLE | 503 | Temporary unavailability | Yes |

**Java DTO**:
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
}
```

---

### PingResponse

Health check response.

**Schema**:
```yaml
PingResponse:
  type: object
  required: [message]
  properties:
    message:
      type: string
      example: pong
```

**Java DTO**:
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PingResponse {
    private String message;
}
```

---

## API Request/Response Models

### CreateUserRequest

Request body for creating a user.

**Schema**:
```yaml
CreateUserRequest:
  type: object
  required: [username, name, password, emailAddress]
  properties:
    username:
      type: string
      minLength: 3
      maxLength: 50
      pattern: '^[a-zA-Z0-9_]+$'
    name:
      type: string
      minLength: 1
      maxLength: 100
    password:
      type: string
      minLength: 8
    emailAddress:
      type: string
      format: email
```

**Java DTO**:
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String username;
    
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;
    
    @NotBlank
    @Size(min = 8)
    private String password;
    
    @NotBlank
    @Email
    private String emailAddress;
}
```

---

### UpdateUserRequest

Request body for updating a user.

**Schema**:
```yaml
UpdateUserRequest:
  type: object
  properties:
    name:
      type: string
      minLength: 1
      maxLength: 100
    password:
      type: string
      minLength: 8
    emailAddress:
      type: string
      format: email
```

**Validation**:
- All fields are optional (partial update)
- At least one field must be provided (validated in controller)
- Cannot update `id` or `username` (immutable)

**Java DTO**:
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @Size(min = 1, max = 100)
    private String name;
    
    @Size(min = 8)
    private String password;
    
    @Email
    private String emailAddress;
}
```

---

### User (Response)

User representation returned in API responses.

**Schema**:
```yaml
User:
  type: object
  required: [id, username, name, emailAddress, roles]
  properties:
    id:
      type: string
      format: uuid
    username:
      type: string
    name:
      type: string
    emailAddress:
      type: string
      format: email
    roles:
      type: array
      items:
        $ref: '#/components/schemas/RoleName'
```

**Note**: Password is NEVER included in response.

**Java DTO**:
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String name;
    private String emailAddress;
    private Set<RoleName> roles;
}
```

---

### UserPage

Paginated list of users with metadata.

**Schema**:
```yaml
UserPage:
  type: object
  required: [content, page, pageSize, totalPages, totalElements]
  properties:
    content:
      type: array
      items:
        $ref: '#/components/schemas/User'
    page:
      type: integer
      minimum: 0
      description: Current page number (0-indexed)
    pageSize:
      type: integer
      minimum: 1
      description: Number of items per page
    totalPages:
      type: integer
      minimum: 0
      description: Total number of pages
    totalElements:
      type: integer
      minimum: 0
      description: Total number of matching users
```

**Java DTO**:
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPage {
    private List<UserResponse> content;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private Long totalElements;
}
```

---

### LoginRequest

Request body for authentication.

**Schema**:
```yaml
LoginRequest:
  type: object
  required: [username, password]
  properties:
    username:
      type: string
    password:
      type: string
      format: password
```

**Java DTO**:
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
}
```

---

### LoginResponse

Response body for successful authentication.

**Schema**:
```yaml
LoginResponse:
  type: object
  required: [token]
  properties:
    token:
      type: string
      description: JWT bearer token
    tokenType:
      type: string
      enum: [Bearer]
      default: Bearer
```

**Java DTO**:
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    
    @Builder.Default
    private String tokenType = "Bearer";
}
```

---

## Relationships Diagram

```
┌─────────────┐
│    User     │
├─────────────┤
│ id (PK)     │
│ username    │
│ name        │
│ password    │
│ emailAddress│
│ createdAt   │
│ updatedAt   │
└──────┬──────┘
       │
       │ 1:N
       │
       ▼
┌─────────────────┐
│   UserRole      │
├─────────────────┤
│ userId (FK)     │◄─── Composite PK
│ roleName        │◄─── Composite PK
└─────────────────┘
       │
       │ N:1 (logical)
       ▼
┌─────────────┐
│   RoleName  │
│   (Enum)    │
├─────────────┤
│ ADMIN       │
│ USER        │
│ GUEST       │
└─────────────┘
```

---

## Database Schema (DDL)

```sql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_users_username ON users(LOWER(username));
CREATE UNIQUE INDEX idx_users_email ON users(LOWER(email_address));

-- User roles join table
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (role_name IN ('ADMIN', 'USER', 'GUEST'))
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_name);
```

---

## Data Validation Summary

### User Entity
- ✅ Username: 3-50 chars, alphanumeric + underscore, unique
- ✅ Name: 1-100 chars, required
- ✅ Password: Min 8 chars, hashed, never returned
- ✅ Email: Valid format, unique, case-insensitive

### Role Assignment
- ✅ Role must be valid enum (ADMIN, USER, GUEST)
- ✅ User must exist
- ✅ Assignment is idempotent
- ✅ Removal is idempotent

### Pagination
- ✅ page >= 0
- ✅ pageSize between 1 and 100
- ✅ Invalid values return 400 VALIDATION_FAILED

---

## State Transitions

### User Lifecycle

```
[New] ──create──> [Active]
                    │
                    ├──update──> [Active (modified)]
                    │
                    ├──assign role──> [Active (with roles)]
                    │
                    ├──remove role──> [Active (roles updated)]
                    │
                    └──delete──> [Deleted]
```

### Authentication Flow

```
[Unauthenticated] ──login (valid)──> [Authenticated with Token]
                                      │
                                      ├──token valid──> [Access Granted]
                                      │
                                      ├──token expired──> [Unauthenticated]
                                      │
                                      └──token invalid──> [Unauthenticated]

[Unauthenticated] ──login (invalid)──> [Unauthenticated (400 error)]
```

### Bootstrap Mode

```
[Zero Users] ──POST /users (no auth)──> [First User Created]
                                         │
                                         └──> [Normal Mode]
                                               (auth required for POST /users)
```

---

## Performance Considerations

### Indexes
- Username and email lookups are indexed (frequent queries)
- Case-insensitive indexes for uniqueness checks
- User-role join table indexed for both directions

### Query Optimization
- Pagination limits result set size
- Filtered queries use indexed columns where possible
- Role joins optimized with proper indexes

### Caching Opportunities
- JWT validation doesn't hit database (stateless)
- User roles could be cached after retrieval
- Role enum is in-memory (no DB access needed)

---

## Future Enhancements

### Potential Additions
- **Audit Log**: Track user modifications (created_by, updated_by)
- **Soft Delete**: Add deleted_at field instead of hard delete
- **Account Status**: Add enabled/disabled flag
- **Password History**: Prevent password reuse
- **Role Permissions**: Replace enum with database-driven roles and permissions
- **User Groups**: Add group entity for bulk role assignment
- **Profile Extensions**: Additional user attributes (phone, address, preferences)
- **Multi-Factor Auth**: Add MFA configuration fields

### Schema Evolution
- All changes via Flyway migrations
- Backward-compatible changes only (additive)
- Never remove or rename existing columns
- New optional fields must have defaults

