# Data Model: User Management API System

**Feature**: User Management API System | **Date**: 2025-12-29 | **Spec**: [spec.md](./spec.md)

## Overview

This document defines the complete data model for the User Management API System, including database entities, API models (DTOs), relationships, validation rules, and state transitions. The data model is designed to support all requirements from spec.md while maintaining consistency with the OpenAPI contract.

## Entity-Relationship Diagram

```
┌─────────────────────┐
│      User           │
│─────────────────────│
│ id (PK, UUID)       │
│ username            │
│ name                │
│ email_address (UK)  │
│ password_hash       │
│ created_at          │
│ updated_at          │
└──────────┬──────────┘
           │
           │ 1
           │
           │ *
┌──────────┴──────────┐
│    UserRole         │ (Join Table)
│─────────────────────│
│ user_id (FK)        │
│ role_id (FK)        │
│ assigned_at         │
└──────────┬──────────┘
           │
           │ *
           │
           │ 1
┌──────────┴──────────┐
│      Role           │
│─────────────────────│
│ id (PK, UUID)       │
│ role_name (UK)      │
│ description         │
└──────────┬──────────┘
           │
           │ 1
           │
           │ *
┌──────────┴──────────┐
│  RolePermission     │ (Join Table)
│─────────────────────│
│ role_id (FK)        │
│ permission_id (FK)  │
└──────────┬──────────┘
           │
           │ *
           │
           │ 1
┌──────────┴──────────┐
│   Permission        │
│─────────────────────│
│ id (PK, UUID)       │
│ permission (UK)     │
│ description         │
└─────────────────────┘
```

**Legend**: PK = Primary Key, FK = Foreign Key, UK = Unique Key

---

## Database Entities

### UserEntity

Represents a user in the system with authentication credentials and profile information.

**Table Name**: `users`

| Column Name     | Type          | Constraints                | Description                              |
|----------------|---------------|----------------------------|------------------------------------------|
| `id`           | UUID          | PRIMARY KEY, NOT NULL      | Unique user identifier (system-assigned) |
| `username`     | VARCHAR(100)  | NOT NULL                   | User's login username                    |
| `name`         | VARCHAR(255)  | NOT NULL                   | User's full display name                 |
| `email_address`| VARCHAR(255)  | UNIQUE, NOT NULL           | User's email (must be unique)            |
| `password_hash`| VARCHAR(255)  | NOT NULL                   | BCrypt hashed password                   |
| `created_at`   | TIMESTAMP     | NOT NULL, DEFAULT NOW()    | Record creation timestamp                |
| `updated_at`   | TIMESTAMP     | NOT NULL, DEFAULT NOW()    | Last update timestamp                    |

**Indexes**:
- Primary key index on `id` (automatic)
- Unique index on `email_address` (enforces uniqueness)
- Non-unique index on `username` (for filtering/search)

**Validation Rules**:
- `username`: Required, max length 100, no whitespace-only
- `name`: Required, max length 255
- `email_address`: Required, valid email format, max length 255, unique
- `password_hash`: Required, BCrypt format (60 characters)

**Java Entity Class**:
```java
@Table("users")
public class UserEntity {
    @Id
    private UUID id;
    
    @Column("username")
    private String username;
    
    @Column("name")
    private String name;
    
    @Column("email_address")
    private String emailAddress;
    
    @Column("password_hash")
    private String passwordHash;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("updated_at")
    private Instant updatedAt;
    
    // Transient field: roles loaded separately
    @Transient
    private List<RoleEntity> roles;
    
    // Constructors, getters, setters
}
```

---

### RoleEntity

Represents a predefined role that can be assigned to users.

**Table Name**: `roles`

| Column Name  | Type         | Constraints           | Description                      |
|-------------|--------------|-----------------------|----------------------------------|
| `id`        | UUID         | PRIMARY KEY, NOT NULL | Unique role identifier           |
| `role_name` | VARCHAR(50)  | UNIQUE, NOT NULL      | Role name (ADMIN, USER, GUEST)   |
| `description` | VARCHAR(255) | NULL                | Human-readable role description  |

**Indexes**:
- Primary key index on `id` (automatic)
- Unique index on `role_name`

**Validation Rules**:
- `role_name`: Required, one of: ADMIN, USER, GUEST
- `description`: Optional, max length 255

**Predefined Roles** (inserted via migration):
- **ADMIN**: Full system access
- **USER**: Standard user access
- **GUEST**: Limited read-only access

**Java Entity Class**:
```java
@Table("roles")
public class RoleEntity {
    @Id
    private UUID id;
    
    @Column("role_name")
    private String roleName;
    
    @Column("description")
    private String description;
    
    // Transient field: permissions loaded separately
    @Transient
    private List<PermissionEntity> permissions;
    
    // Constructors, getters, setters
}
```

---

### PermissionEntity

Represents a specific permission that can be associated with roles.

**Table Name**: `permissions`

| Column Name   | Type          | Constraints           | Description                       |
|--------------|---------------|-----------------------|-----------------------------------|
| `id`         | UUID          | PRIMARY KEY, NOT NULL | Unique permission identifier      |
| `permission` | VARCHAR(100)  | UNIQUE, NOT NULL      | Permission code (e.g., users:read)|
| `description`| VARCHAR(255)  | NULL                  | Human-readable description        |

**Indexes**:
- Primary key index on `id` (automatic)
- Unique index on `permission`

**Validation Rules**:
- `permission`: Required, format: `resource:action`, max length 100
- `description`: Optional, max length 255

**Predefined Permissions** (inserted via migration):
- `users:read`: Read user data
- `users:write`: Create and update users
- `users:delete`: Delete users
- `roles:assign`: Assign roles to users

**Java Entity Class**:
```java
@Table("permissions")
public class PermissionEntity {
    @Id
    private UUID id;
    
    @Column("permission")
    private String permission;
    
    @Column("description")
    private String description;
    
    // Constructors, getters, setters
}
```

---

### UserRoleEntity (Join Table)

Represents the many-to-many relationship between users and roles.

**Table Name**: `user_roles`

| Column Name  | Type      | Constraints                        | Description                   |
|-------------|-----------|-------------------------------------|-------------------------------|
| `user_id`   | UUID      | FK → users(id), NOT NULL, ON DELETE CASCADE | References user          |
| `role_id`   | UUID      | FK → roles(id), NOT NULL, ON DELETE CASCADE | References role          |
| `assigned_at` | TIMESTAMP | NOT NULL, DEFAULT NOW()           | When role was assigned        |

**Primary Key**: Composite key on `(user_id, role_id)`

**Indexes**:
- Composite primary key index on `(user_id, role_id)` (automatic)
- Non-unique index on `user_id` (for fast lookup of user's roles)
- Non-unique index on `role_id` (for fast lookup of users with a role)

**Cascade Rules**:
- When user deleted: Delete all role assignments (CASCADE)
- When role deleted: Delete all assignments of that role (CASCADE)

**Java Entity Class**:
```java
@Table("user_roles")
public class UserRoleEntity {
    @Column("user_id")
    private UUID userId;
    
    @Column("role_id")
    private UUID roleId;
    
    @Column("assigned_at")
    private Instant assignedAt;
    
    // Composite key handled via @Id on both fields or @IdClass
    // Constructors, getters, setters
}
```

---

### RolePermissionEntity (Join Table)

Represents the many-to-many relationship between roles and permissions.

**Table Name**: `role_permissions`

| Column Name     | Type | Constraints                               | Description                 |
|----------------|------|-------------------------------------------|-----------------------------|
| `role_id`      | UUID | FK → roles(id), NOT NULL, ON DELETE CASCADE | References role           |
| `permission_id`| UUID | FK → permissions(id), NOT NULL, ON DELETE CASCADE | References permission |

**Primary Key**: Composite key on `(role_id, permission_id)`

**Indexes**:
- Composite primary key index on `(role_id, permission_id)` (automatic)

**Cascade Rules**:
- When role deleted: Delete all permission mappings (CASCADE)
- When permission deleted: Delete all role mappings (CASCADE)

**Java Entity Class**:
```java
@Table("role_permissions")
public class RolePermissionEntity {
    @Column("role_id")
    private UUID roleId;
    
    @Column("permission_id")
    private UUID permissionId;
    
    // Composite key handled via @Id on both fields or @IdClass
    // Constructors, getters, setters
}
```

---

## API Models (DTOs)

These models are **generated from the OpenAPI contract** and represent the API's external interface. They differ from database entities to provide API stability and hide internal structure.

### User (Response DTO)

Represents a user in API responses. Password is NEVER included.

**Generated From**: `openapi.yaml` schema `User`

```yaml
User:
  type: object
  required:
    - id
    - username
    - name
    - emailAddress
    - roles
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
        $ref: '#/components/schemas/Role'
```

**Java Class** (Generated):
```java
public class User {
    private UUID id;
    private String username;
    private String name;
    private String emailAddress;
    private List<Role> roles;
    // getters, setters, equals, hashCode
}
```

**Mapping**: `UserEntity` → `User`
- Copy all fields except `password_hash`, `created_at`, `updated_at`
- Load and map associated `RoleEntity` list to `Role` list

---

### CreateUserRequest (Request DTO)

Represents the request body for creating a new user.

**Generated From**: `openapi.yaml` schema `CreateUserRequest`

```yaml
CreateUserRequest:
  type: object
  required:
    - username
    - name
    - password
    - emailAddress
  properties:
    username:
      type: string
      minLength: 1
      maxLength: 100
    name:
      type: string
      minLength: 1
      maxLength: 255
    password:
      type: string
      minLength: 1
      maxLength: 255
      writeOnly: true
    emailAddress:
      type: string
      format: email
      maxLength: 255
```

**Java Class** (Generated):
```java
public class CreateUserRequest {
    private String username;
    private String name;
    private String password;  // plain text, hashed before storage
    private String emailAddress;
    // getters, setters, validation annotations
}
```

**Mapping**: `CreateUserRequest` → `UserEntity`
- Hash `password` using BCrypt → `password_hash`
- Copy other fields directly
- Generate new UUID for `id`
- Set `created_at` and `updated_at` to current timestamp

**Validation Rules**:
- All fields required
- `username`: 1-100 characters
- `name`: 1-255 characters
- `password`: 1-255 characters (hashed before storage)
- `emailAddress`: Valid email format, max 255 characters, must not exist in database

---

### UpdateUserRequest (Request DTO)

Represents the request body for updating a user.

**Generated From**: `openapi.yaml` schema `UpdateUserRequest`

```yaml
UpdateUserRequest:
  type: object
  properties:
    username:
      type: string
      minLength: 1
      maxLength: 100
    name:
      type: string
      minLength: 1
      maxLength: 255
    password:
      type: string
      minLength: 1
      maxLength: 255
      writeOnly: true
    emailAddress:
      type: string
      format: email
      maxLength: 255
```

**Java Class** (Generated):
```java
public class UpdateUserRequest {
    private String username;      // optional
    private String name;          // optional
    private String password;      // optional, plain text
    private String emailAddress;  // optional
    // getters, setters, validation annotations
}
```

**Mapping**: `UpdateUserRequest` → `UserEntity`
- Only update fields that are present (not null)
- Hash `password` if provided → `password_hash`
- Update `updated_at` to current timestamp
- Validate email uniqueness if changed

**Validation Rules**:
- All fields optional (partial update)
- If provided, same constraints as CreateUserRequest
- Cannot update `id` (immutable)
- Email uniqueness checked if changed

---

### LoginRequest (Request DTO)

Represents login credentials.

**Generated From**: `openapi.yaml` schema `LoginRequest`

```yaml
LoginRequest:
  type: object
  required:
    - username
    - password
  properties:
    username:
      type: string
    password:
      type: string
      writeOnly: true
```

**Java Class** (Generated):
```java
public class LoginRequest {
    private String username;
    private String password;  // plain text, validated against hash
    // getters, setters
}
```

**Validation Rules**:
- Both fields required
- No format validation (accept any string)

**Processing**:
1. Look up user by `username` (or email if spec allows)
2. If user not found: Return generic authentication error
3. If user found: Verify `password` against `password_hash` using BCrypt
4. If match: Generate JWT token
5. If no match: Return generic authentication error (same as not found)

---

### LoginResponse (Response DTO)

Represents successful login response with bearer token.

**Generated From**: `openapi.yaml` schema `LoginResponse`

```yaml
LoginResponse:
  type: object
  required:
    - token
    - tokenType
  properties:
    token:
      type: string
      description: JWT bearer token
    tokenType:
      type: string
      enum: [Bearer]
      default: Bearer
```

**Java Class** (Generated):
```java
public class LoginResponse {
    private String token;
    private TokenTypeEnum tokenType = TokenTypeEnum.BEARER;
    // getters, setters
    
    public enum TokenTypeEnum {
        BEARER("Bearer");
        private String value;
        // enum methods
    }
}
```

---

### UserPage (Response DTO)

Represents a paginated list of users with metadata.

**Generated From**: `openapi.yaml` schema `UserPage`

```yaml
UserPage:
  type: object
  required:
    - items
    - page
    - pageSize
    - totalCount
    - totalPages
  properties:
    items:
      type: array
      items:
        $ref: '#/components/schemas/User'
    page:
      type: integer
      minimum: 1
    pageSize:
      type: integer
      minimum: 1
      maximum: 100
    totalCount:
      type: integer
      minimum: 0
    totalPages:
      type: integer
      minimum: 0
```

**Java Class** (Generated):
```java
public class UserPage {
    private List<User> items;
    private Integer page;
    private Integer pageSize;
    private Long totalCount;
    private Integer totalPages;
    // getters, setters
}
```

**Calculation**:
- `totalPages = ceil(totalCount / pageSize)`
- `items = query with LIMIT pageSize OFFSET (page - 1) * pageSize`

---

### Role (Response DTO)

Represents a role with permissions.

**Generated From**: `openapi.yaml` schema `Role`

```yaml
Role:
  type: object
  required:
    - roleName
    - permissions
  properties:
    roleName:
      $ref: '#/components/schemas/RoleName'
    permissions:
      type: array
      items:
        $ref: '#/components/schemas/Permission'
```

**Java Class** (Generated):
```java
public class Role {
    private RoleNameEnum roleName;
    private List<PermissionEnum> permissions;
    // getters, setters
}
```

**Mapping**: `RoleEntity` + `PermissionEntity` → `Role`
- Map `role_name` to enum
- Load permissions via join table
- Map permission strings to enum values

---

### RoleName (Enum)

Enumeration of valid role names.

**Generated From**: `openapi.yaml` schema `RoleName`

```yaml
RoleName:
  type: string
  enum:
    - ADMIN
    - USER
    - GUEST
```

**Java Enum** (Generated):
```java
public enum RoleNameEnum {
    ADMIN("ADMIN"),
    USER("USER"),
    GUEST("GUEST");
    
    private String value;
    // enum methods
}
```

---

### Permission (Enum)

Enumeration of valid permissions.

**Generated From**: `openapi.yaml` schema `Permission`

```yaml
Permission:
  type: string
  enum:
    - users:read
    - users:write
    - users:delete
    - roles:assign
```

**Java Enum** (Generated):
```java
public enum PermissionEnum {
    USERS_READ("users:read"),
    USERS_WRITE("users:write"),
    USERS_DELETE("users:delete"),
    ROLES_ASSIGN("roles:assign");
    
    private String value;
    // enum methods
}
```

---

### ErrorResponse (Response DTO)

Standard error response for all 4xx and 5xx errors.

**Generated From**: `openapi.yaml` schema `ErrorResponse`

```yaml
ErrorResponse:
  type: object
  required:
    - code
    - message
  properties:
    code:
      type: string
      description: Stable error code
    message:
      type: string
      description: Human-readable error message
```

**Java Class** (Generated):
```java
public class ErrorResponse {
    private String code;
    private String message;
    // getters, setters
}
```

**Error Codes**:
- `VALIDATION_FAILED`: Request validation failed
- `RESOURCE_NOT_FOUND`: Requested resource not found
- `CONFLICT`: Resource conflict (duplicate email)
- `AUTHENTICATION_REQUIRED`: No bearer token provided
- `AUTHENTICATION_FAILED`: Invalid credentials or token
- `INTERNAL_ERROR`: Unexpected server error
- `SERVICE_UNAVAILABLE`: Temporary unavailability

---

### PingResponse (Response DTO)

Simple health check response.

**Generated From**: `openapi.yaml` schema `PingResponse`

```yaml
PingResponse:
  type: object
  required:
    - message
  properties:
    message:
      type: string
```

**Java Class** (Generated):
```java
public class PingResponse {
    private String message;
    // getters, setters
}
```

**Example**:
```json
{
  "message": "pong"
}
```

---

## Relationships

### User ↔ Role (Many-to-Many)

- A user can have zero or more roles
- A role can be assigned to zero or more users
- Join table: `user_roles`
- Cascade delete: When user deleted, remove all their role assignments
- Idempotent operations: Assign/remove role multiple times has same effect

**Loading Strategy**:
- Load user's roles on demand (not always loaded)
- Use JOIN query when roles needed: `SELECT u.*, r.* FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id LEFT JOIN roles r ON ur.role_id = r.id WHERE u.id = ?`

---

### Role ↔ Permission (Many-to-Many)

- A role has one or more permissions
- A permission can belong to one or more roles
- Join table: `role_permissions`
- Predefined mappings (not modified via API)

**Permission Assignments** (Predefined):
- **ADMIN**: All permissions (users:read, users:write, users:delete, roles:assign)
- **USER**: users:read, users:write
- **GUEST**: users:read

---

## State Transitions

### User Lifecycle

```
┌─────────────────────┐
│   Does Not Exist    │
└──────────┬──────────┘
           │
           │ POST /users
           │ (CreateUserRequest)
           │
           ▼
┌─────────────────────┐
│   Active User       │ ◄──────┐
│   (has ID, roles)   │        │
└──────┬───────┬──────┘        │
       │       │                │
       │       │ PUT /users/{id}│
       │       │ (UpdateUserRequest)
       │       └────────────────┘
       │
       │ DELETE /users/{id}
       │
       ▼
┌─────────────────────┐
│     Deleted         │
│  (404 on GET)       │
└─────────────────────┘
```

**States**:
1. **Does Not Exist**: User not in database, cannot be retrieved
2. **Active User**: User exists, can be retrieved, updated, deleted, assigned roles
3. **Deleted**: User removed from database, all role assignments cascaded

**Transitions**:
- Create: Does Not Exist → Active User
- Update: Active User → Active User (modified)
- Delete: Active User → Deleted

**No Soft Delete**: Users are hard-deleted (removed from database)

---

### Role Assignment Lifecycle

```
┌─────────────────────┐
│  Not Assigned       │
└──────────┬──────────┘
           │
           │ PUT /users/{userId}/roles/{roleName}
           │
           ▼
┌─────────────────────┐
│    Assigned         │
│  (in user_roles)    │
└──────────┬──────────┘
           │
           │ DELETE /users/{userId}/roles/{roleName}
           │
           ▼
┌─────────────────────┐
│  Not Assigned       │
└─────────────────────┘
```

**States**:
1. **Not Assigned**: No row in `user_roles` for (user_id, role_id)
2. **Assigned**: Row exists in `user_roles`

**Transitions**:
- Assign: Not Assigned → Assigned (idempotent: if already assigned, no-op)
- Remove: Assigned → Not Assigned (idempotent: if not assigned, no-op)

**Cascade**: User deletion removes all role assignments

---

## Validation Rules Summary

### User Fields

| Field         | Create | Update | Validation                                    |
|--------------|--------|--------|-----------------------------------------------|
| `username`   | REQ    | OPT    | 1-100 chars, not whitespace-only              |
| `name`       | REQ    | OPT    | 1-255 chars                                   |
| `password`   | REQ    | OPT    | 1-255 chars, hashed before storage            |
| `emailAddress` | REQ  | OPT    | Valid email, max 255 chars, unique            |

**Legend**: REQ = Required, OPT = Optional

### Role Assignment

| Field      | Validation                                    |
|-----------|-----------------------------------------------|
| `userId`  | Must be valid UUID of existing user          |
| `roleName`| Must be one of: ADMIN, USER, GUEST           |

### Pagination

| Parameter  | Validation                                    |
|-----------|-----------------------------------------------|
| `page`    | Required, integer ≥ 1                         |
| `pageSize`| Required, integer 1-100                       |

### Login

| Field      | Validation                                    |
|-----------|-----------------------------------------------|
| `username`| Required, any string                          |
| `password`| Required, any string                          |

---

## Database Constraints

### Primary Keys
- All entities use UUID primary keys
- Generated by database (H2: `random_uuid()`)
- Never null, never changeable

### Unique Constraints
- `users.email_address`: Enforces email uniqueness across all users
- `roles.role_name`: Enforces role name uniqueness
- `permissions.permission`: Enforces permission uniqueness
- Composite PK on join tables prevents duplicate mappings

### Foreign Keys
- `user_roles.user_id` → `users.id` (ON DELETE CASCADE)
- `user_roles.role_id` → `roles.id` (ON DELETE CASCADE)
- `role_permissions.role_id` → `roles.id` (ON DELETE CASCADE)
- `role_permissions.permission_id` → `permissions.id` (ON DELETE CASCADE)

### Check Constraints
- None required (validation handled at application layer)

### Indexes (Performance)
- `idx_users_email`: On `users.email_address` (unique login lookups)
- `idx_users_username`: On `users.username` (filtering)
- `idx_user_roles_user`: On `user_roles.user_id` (load user's roles)
- `idx_user_roles_role`: On `user_roles.role_id` (find users with role)

---

## Mapping Strategy

### Entity ↔ DTO Conversion

**Principle**: API models (DTOs) are stable; entities can evolve

**Mappers**:
1. **UserMapper**: Converts between `UserEntity` and `User/CreateUserRequest/UpdateUserRequest`
2. **RoleMapper**: Converts between `RoleEntity` and `Role`

**Rules**:
- Password hashing happens in mapper during creation
- Passwords never mapped to DTOs
- Timestamps not exposed in DTOs (internal concern)
- Roles loaded separately and mapped as needed

**Example Mapping** (Create):
```java
public UserEntity toEntity(CreateUserRequest request) {
    UserEntity entity = new UserEntity();
    entity.setId(UUID.randomUUID());
    entity.setUsername(request.getUsername());
    entity.setName(request.getName());
    entity.setEmailAddress(request.getEmailAddress());
    entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    entity.setCreatedAt(Instant.now());
    entity.setUpdatedAt(Instant.now());
    return entity;
}
```

**Example Mapping** (To DTO):
```java
public User toDto(UserEntity entity, List<RoleEntity> roles) {
    User dto = new User();
    dto.setId(entity.getId());
    dto.setUsername(entity.getUsername());
    dto.setName(entity.getName());
    dto.setEmailAddress(entity.getEmailAddress());
    // Password NOT mapped
    dto.setRoles(roles.stream()
        .map(roleMapper::toDto)
        .collect(Collectors.toList()));
    return dto;
}
```

---

## Migration Scripts

### V001__init_schema.sql

```sql
-- Users table
CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT random_uuid(),
    username     VARCHAR(100) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email_address);
CREATE INDEX idx_users_username ON users(username);
```

### V002__roles_permissions.sql

```sql
-- Roles table
CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT random_uuid(),
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Permissions table
CREATE TABLE permissions (
    id          UUID PRIMARY KEY DEFAULT random_uuid(),
    permission  VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Role-Permission mapping
CREATE TABLE role_permissions (
    role_id      UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Insert predefined roles
INSERT INTO roles (id, role_name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN', 'Full system access'),
    ('00000000-0000-0000-0000-000000000002', 'USER', 'Standard user access'),
    ('00000000-0000-0000-0000-000000000003', 'GUEST', 'Limited read-only access');

-- Insert predefined permissions
INSERT INTO permissions (id, permission, description) VALUES
    ('00000000-0000-0000-0000-000000000011', 'users:read', 'Read user data'),
    ('00000000-0000-0000-0000-000000000012', 'users:write', 'Create and update users'),
    ('00000000-0000-0000-0000-000000000013', 'users:delete', 'Delete users'),
    ('00000000-0000-0000-0000-000000000014', 'roles:assign', 'Assign roles to users');

-- Assign permissions to roles
-- ADMIN: all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000001', id FROM permissions;

-- USER: read and write
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000011'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000012');

-- GUEST: read only
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000011');
```

### V003__users_api.sql

```sql
-- User-Role mapping
CREATE TABLE user_roles (
    user_id     UUID NOT NULL,
    role_id     UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
```

---

## Data Model Compliance Checklist

- ✅ All entities have UUIDs as primary keys
- ✅ Email uniqueness enforced at database level
- ✅ Passwords hashed with BCrypt (never stored plain text)
- ✅ Timestamps track creation and updates
- ✅ Join tables for many-to-many relationships
- ✅ Cascade deletes configured (user deletion removes role assignments)
- ✅ Indexes on frequently queried columns
- ✅ API models separate from database entities
- ✅ Predefined roles and permissions seeded via migrations
- ✅ No `retryable` field in error responses
- ✅ Pagination metadata included in list responses
- ✅ Password field write-only (never in responses)

---

**Data Model Status**: ✅ Complete  
**Next Phase**: Phase 2 - Implementation  
**Last Updated**: 2025-12-29

