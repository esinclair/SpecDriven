# Data Model: User Management API System

**Feature**: User Management API System | **Date**: 2025-12-30 | **Spec**: [spec.md](./spec.md)

## Overview

This document defines the data entities, relationships, validation rules, and state transitions for the User Management API System. The model supports user lifecycle management, role-based access control, and JWT authentication.

## Entity Relationship Diagram

```
┌──────────────────┐          ┌──────────────────┐
│      User        │          │      Role        │
├──────────────────┤          ├──────────────────┤
│ id (UUID) PK     │          │ id (UUID) PK     │
│ username         │          │ roleName         │
│ name             │          │ permissions[]    │
│ emailAddress UQ  │          └──────────────────┘
│ passwordHash     │                   │
│ createdAt        │                   │
│ updatedAt        │                   │
└──────────────────┘                   │
         │                             │
         │         ┌──────────────────────────┐
         └────────►│    UserRole (Join)       │
                   ├──────────────────────────┤
                   │ userId FK                │
                   │ roleId FK                │
                   │ assignedAt               │
                   └──────────────────────────┘
                            PK: (userId, roleId)
```

## Core Entities

### User

**Description**: Represents a person or principal in the system with authentication credentials and assigned roles.

**Table**: `users`

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, NOT NULL, Auto-generated | Unique identifier for the user |
| `username` | VARCHAR(255) | NOT NULL | Username for the user (not unique) |
| `name` | VARCHAR(255) | NOT NULL | Full name or display name |
| `emailAddress` | VARCHAR(255) | UNIQUE, NOT NULL | Email address (unique constraint enforced) |
| `passwordHash` | VARCHAR(255) | NOT NULL | BCrypt hash of user's password (never exposed in API) |
| `createdAt` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updatedAt` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Relationships**:
- One User has many UserRole assignments (many-to-many with Role through UserRole)

**Validation Rules** (API Level - Jakarta Bean Validation):
- `username`: Required (@NotBlank), max 255 characters (@Size)
- `name`: Required (@NotBlank), max 255 characters (@Size)
- `emailAddress`: Required (@NotBlank), valid email format (@Email), max 255 characters (@Size), unique (business logic)
- `password` (request only): Required on creation (@NotBlank), min 8 characters (@Size), never returned in response

**Business Rules**:
- Email addresses must be unique across all users (enforced by unique constraint)
- Password must be hashed using BCrypt before storage
- `id` is system-assigned and cannot be modified by clients
- `passwordHash` is never included in API responses
- `createdAt` and `updatedAt` are managed by the system
- User can be created without authentication only if database is empty (bootstrap mode)
- User deletion cascades to all UserRole assignments

**State Transitions**:
- **Created**: User is created with initial data, password is hashed, timestamps set
- **Updated**: User fields can be modified (except id, createdAt), updatedAt timestamp updated
- **Deleted**: User record is removed, all associated UserRole assignments are deleted

**Indexes**:
- Primary key index on `id` (automatic)
- Unique index on `emailAddress` (explicit)
- Index on `username` (for filtering)

---

### Role

**Description**: Represents a named permission group that can be assigned to users. Roles are predefined by the system.

**Table**: `roles`

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, NOT NULL, Auto-generated | Unique identifier for the role |
| `roleName` | VARCHAR(50) | UNIQUE, NOT NULL | Name of the role (e.g., ADMIN, USER, GUEST) |
| `description` | VARCHAR(500) | NULL | Human-readable description of the role |
| `createdAt` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |

**Relationships**:
- One Role can be assigned to many Users through UserRole (many-to-many)
- One Role has many Permission associations (modeled as JSON array or separate table)

**Validation Rules**:
- `roleName`: Required (@NotBlank), must match predefined set (custom validation)
- `description`: Optional, max 500 characters

**Business Rules**:
- Roles are predefined and seeded during database initialization
- API consumers cannot create, update, or delete roles
- Only predefined role names are valid for assignment
- Initial role set includes: ADMIN, USER, GUEST
- Role names are case-sensitive

**Predefined Roles**:
1. **ADMIN**: Full system access (all permissions)
2. **USER**: Standard user access (read/write own data)
3. **GUEST**: Read-only access (limited permissions)

**Permissions** (per role):
- Permissions are represented as string values
- Stored as JSON array or in separate permissions table
- Example permissions: `users:read`, `users:write`, `users:delete`, `roles:assign`

---

### UserRole (Join Table)

**Description**: Represents the many-to-many relationship between Users and Roles. Tracks role assignments to users.

**Table**: `user_roles`

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `userId` | UUID | FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE, NOT NULL | User receiving the role |
| `roleId` | UUID | FOREIGN KEY REFERENCES roles(id) ON DELETE CASCADE, NOT NULL | Role being assigned |
| `assignedAt` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | When the role was assigned |

**Composite Primary Key**: (`userId`, `roleId`)

**Relationships**:
- Many UserRole entries belong to one User
- Many UserRole entries belong to one Role

**Business Rules**:
- A user cannot have the same role assigned multiple times (enforced by composite primary key)
- Assigning a role that is already assigned is idempotent (no error)
- Removing a role that is not assigned is idempotent (no error)
- When a user is deleted, all their role assignments are deleted (CASCADE)
- When a role is deleted, all assignments of that role are deleted (CASCADE)

**Indexes**:
- Composite primary key index on (`userId`, `roleId`) (automatic)
- Foreign key index on `userId` (automatic)
- Foreign key index on `roleId` (automatic)

---

## API-Only Models (Not Persisted)

### LoginRequest

**Description**: Request payload for user authentication.

**Fields**:
- `username` (string, required): Username for authentication
- `password` (string, required): Password for authentication

**Validation**:
- Both fields are required (@NotBlank)
- No format validation beyond non-empty (authentication validates credentials)

---

### LoginResponse

**Description**: Response payload for successful authentication.

**Fields**:
- `token` (string, required): JWT bearer token
- `tokenType` (string, optional): Token type, typically "Bearer"
- `expiresIn` (integer, optional): Token expiration in seconds

---

### ErrorResponse

**Description**: Standard error payload for all 4xx and 5xx responses.

**Fields**:
- `code` (string, required): Stable error identifier (e.g., VALIDATION_FAILED, RESOURCE_NOT_FOUND)
- `message` (string, required): Human-readable description safe for display
- `details` (object, optional): Additional diagnostic information (e.g., field-level validation errors)

**Error Codes**:
- `VALIDATION_FAILED`: Request validation failed (400)
- `RESOURCE_NOT_FOUND`: Requested resource not found (404)
- `CONFLICT`: Resource conflict (e.g., duplicate email) (409)
- `AUTHENTICATION_REQUIRED`: Authentication required but not provided (401)
- `AUTHENTICATION_FAILED`: Authentication credentials invalid (401)
- `INTERNAL_ERROR`: Unexpected internal error (500)
- `SERVICE_UNAVAILABLE`: Temporary service unavailability (503)
- `FEATURE_DISABLED`: Feature is disabled via feature flag (404)

---

### PagedResult<T>

**Description**: Generic wrapper for paginated list responses.

**Fields**:
- `items` (array of T, required): Array of items for the current page
- `page` (integer, required): Current page number (1-based)
- `pageSize` (integer, required): Number of items per page
- `totalCount` (integer, required): Total number of items across all pages
- `totalPages` (integer, required): Total number of pages

**Pagination Parameters** (query parameters):
- `page` (integer, required): Page number (1-based, minimum 1)
- `pageSize` (integer, required): Items per page (minimum 1, maximum 100)

---

## Validation Rules Summary

### Field-Level Validations (Jakarta Bean Validation)

**User Creation/Update**:
- `username`: @NotBlank, @Size(max=255)
- `name`: @NotBlank, @Size(max=255)
- `emailAddress`: @NotBlank, @Email, @Size(max=255)
- `password` (creation only): @NotBlank, @Size(min=8, max=255)

**Login**:
- `username`: @NotBlank
- `password`: @NotBlank

**Role Assignment**:
- `roleName`: @NotBlank, @Pattern(regexp="ADMIN|USER|GUEST")

**Pagination**:
- `page`: @Min(1)
- `pageSize`: @Min(1), @Max(100)

### Business-Level Validations

**User**:
- Email uniqueness (409 CONFLICT if duplicate)
- User existence (404 RESOURCE_NOT_FOUND if not found)
- Authentication requirement (401 AUTHENTICATION_REQUIRED if not authenticated)
- Password never returned in responses

**Role**:
- Role name must match predefined set (400 VALIDATION_FAILED if unknown)
- Role existence when assigning (400 VALIDATION_FAILED if invalid)

**UserRole**:
- User must exist when assigning role (404 RESOURCE_NOT_FOUND)
- Role must be valid when assigning (400 VALIDATION_FAILED)
- Idempotent assignment (no error if role already assigned)
- Idempotent removal (no error if role not assigned)

---

## State Transitions

### User Lifecycle

```
[Non-Existent]
       │
       │ POST /users (with valid data)
       ▼
   [Created]
       │
       │ PUT /users/{id} (with valid data)
       ▼
   [Updated]
       │
       │ DELETE /users/{id}
       ▼
   [Deleted]
       │
       ▼
[Non-Existent]
```

**Transitions**:
1. **Creation**: POST /users with valid user data → 201 Created with user object
2. **Retrieval**: GET /users/{id} → 200 OK with user object (password excluded)
3. **Update**: PUT /users/{id} with valid data → 200 OK with updated user object
4. **Deletion**: DELETE /users/{id} → 204 No Content
5. **List**: GET /users with pagination → 200 OK with paged result

**Error Transitions**:
- Invalid data → 400 VALIDATION_FAILED
- Duplicate email → 409 CONFLICT
- User not found → 404 RESOURCE_NOT_FOUND
- Not authenticated → 401 AUTHENTICATION_REQUIRED

---

### Role Assignment Lifecycle

```
[User without Role]
       │
       │ POST /users/{userId}/roles/{roleName}
       ▼
[User with Role]
       │
       │ DELETE /users/{userId}/roles/{roleName}
       ▼
[User without Role]
```

**Transitions**:
1. **Assignment**: POST /users/{userId}/roles/{roleName} → 204 No Content
2. **Removal**: DELETE /users/{userId}/roles/{roleName} → 204 No Content
3. **Retrieval**: GET /users/{id} includes roles array in response

**Idempotency**:
- Assigning an already-assigned role → 204 No Content (no error)
- Removing a non-assigned role → 204 No Content (no error)

---

## Database Schema

### Migration Script Structure

**V001__create_users_table.sql**:
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    username VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email_address);
```

**V002__create_roles_table.sql**:
```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed predefined roles
INSERT INTO roles (id, role_name, description) VALUES
    ('11111111-1111-1111-1111-111111111111', 'ADMIN', 'Full system access with all permissions'),
    ('22222222-2222-2222-2222-222222222222', 'USER', 'Standard user access'),
    ('33333333-3333-3333-3333-333333333333', 'GUEST', 'Read-only access');
```

**V003__create_user_roles_table.sql**:
```sql
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
```

---

## Mapping Between Entities and DTOs

### User Entity → User DTO (Response)
- `id` → `id`
- `username` → `username`
- `name` → `name`
- `emailAddress` → `emailAddress`
- `passwordHash` → **EXCLUDED** (never in response)
- `createdAt` → `createdAt`
- `updatedAt` → `updatedAt`
- Fetched roles → `roles` (array of role names)

### User DTO (Request) → User Entity
- `username` → `username`
- `name` → `name`
- `emailAddress` → `emailAddress`
- `password` → Hash using BCrypt → `passwordHash`
- `id` → Generated by database
- `createdAt`, `updatedAt` → Set by database

### Role Entity → Role DTO
- `id` → `id`
- `roleName` → `roleName`
- `description` → `description`
- Permissions (from JSON or separate table) → `permissions` array

---

## Performance Considerations

### Indexing Strategy
- **Primary Keys**: Automatic indexes on UUIDs (fast lookup)
- **Unique Constraints**: email_address has unique index (fast duplicate checking)
- **Foreign Keys**: Automatic indexes on user_roles join table
- **Filtering**: Index on username for list filtering

### Query Optimization
- **User CRUD**: Single-row operations use primary key index (O(1) lookup)
- **List Users**: Pagination with LIMIT/OFFSET reduces result set
- **Role Lookup**: Small, rarely-changing table (can be cached)
- **Authentication**: Single query to find user by username, then in-memory JWT validation

### Performance Targets (from spec)
- Health check: < 1s (95th percentile) - **No database query**
- Login: < 1s (95th percentile) - **Single user lookup + JWT generation**
- User CRUD: < 1s (95th percentile) - **Single-row operations with indexes**
- List users: < 1s (95th percentile) - **Paginated queries with max page size 100**

---

## Security Considerations

### Password Security
- Passwords are hashed using BCrypt before storage
- BCrypt cost factor: 10 (configurable)
- Plaintext passwords never stored in database
- Password field is write-only (never returned in API responses)

### Email Enumeration Prevention
- Login errors for "unknown username" and "incorrect password" return identical responses
- Same error code and message for both scenarios
- No timing differences (use constant-time comparison where possible)

### Authentication Token Security
- JWT tokens are signed with HMAC SHA-256
- Tokens include expiration time (configurable, default 24 hours)
- Expired tokens are rejected with 401 error
- Token secret must be strong and environment-specific

### Input Validation
- All user inputs validated using Bean Validation
- SQL injection prevented by parameterized queries (Spring Data JDBC)
- XSS prevention: API returns JSON only (no HTML rendering)

---

## Edge Cases and Business Rules

### Email Uniqueness
- Email addresses are case-insensitive for uniqueness (normalize to lowercase before checking)
- Creating/updating with duplicate email returns 409 CONFLICT
- Error message: "Email address already exists"

### Bootstrap Mode
- First user can be created without authentication (when database is empty)
- After first user exists, all operations require authentication
- Checked by counting users in database

### Pagination Boundaries
- Requesting page beyond available data returns empty result (not an error)
- totalCount and totalPages remain accurate
- Example: If totalCount=50 and pageSize=20, page=4 returns empty items array

### Role Assignment Idempotency
- Assigning already-assigned role: 204 No Content (no change, no error)
- Removing non-assigned role: 204 No Content (no change, no error)
- Simplifies client logic (can safely retry operations)

### Concurrent Updates
- Last write wins for user updates (no optimistic locking in initial version)
- Future enhancement: Add version field for optimistic locking if needed

### User Deletion Cascade
- Deleting a user removes all their role assignments (ON DELETE CASCADE)
- No orphaned user_roles records
- Idempotent: Deleting non-existent user returns 404

### Feature Flag Behavior
- When FeatureFlag.usersApi=false, all user endpoints return 404
- Error message does not reveal feature existence: "Resource not found"
- Health check endpoint always available (not gated)

---

## Summary

This data model provides:
- **Simple, normalized schema** with clear relationships
- **Comprehensive validation** at database, entity, and API levels
- **Security** through password hashing and write-only password fields
- **Performance** through appropriate indexing and pagination
- **Idempotency** for role assignment operations
- **Scalability** via pagination and indexed queries
- **Testability** through clear state transitions and validation rules

All design decisions align with:
- Feature specification requirements
- Constitution principles (especially Spring-First Development)
- Performance budget (≤1s for synchronous operations)
- Error handling requirements (stable codes, HTTP semantics)
- Security best practices
