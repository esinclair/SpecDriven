# Quickstart Guide: User Management API System

**Feature**: User Management API System | **Date**: 2025-12-29 | **Spec**: [spec.md](./spec.md)

## Overview

This quickstart guide helps you get the User Management API System running locally, make your first API calls, and understand the core workflows. Follow these steps to be up and running in under 5 minutes.

---

## Prerequisites

Before you begin, ensure you have:

- **Java 17 JDK** installed (check with `java -version`)
- **Git** (to clone the repository if needed)
- **cURL** or **Postman** (for making API requests)
- **Text editor or IDE** (IntelliJ IDEA, VS Code, etc.)

**Optional**:
- **Gradle** (wrapper included, so not required)
- **H2 Console** (built-in, accessible via browser)

---

## Quick Start (5 Minutes)

### Step 1: Clone and Build

```bash
# Clone repository (if not already done)
git clone <repository-url>
cd SpecDriven

# Build the project (runs tests, generates OpenAPI code)
./gradlew clean build

# Expected output: BUILD SUCCESSFUL
```

**What happens during build**:
- OpenAPI Generator creates Java interfaces and DTOs from `openapi.yaml`
- Compiles all source code
- Runs unit and integration tests
- Packages application as executable JAR

---

### Step 2: Enable the Users API Feature

By default, the Users API is **disabled** via feature flag. Enable it:

**Option A**: Edit `src/main/resources/application.yml`:
```yaml
FeatureFlag:
  usersApi: true  # Change from false to true
```

**Option B**: Set environment variable:
```bash
export FEATURE_FLAG_USERS_API=true
```

**Option C**: Pass as command-line argument:
```bash
./gradlew bootRun --args='--FeatureFlag.usersApi=true'
```

---

### Step 3: Start the Application

```bash
# Run the application
./gradlew bootRun

# Alternative: Run the built JAR
java -jar build/libs/SpecDriven-0.0.1-SNAPSHOT.jar
```

**Expected output**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::       (v3.5.9)

...
Started SpecDrivenApplication in 2.345 seconds
```

The API is now running at **http://localhost:8080**

---

### Step 4: Verify Health Check

Test the health check endpoint (no authentication required):

```bash
curl http://localhost:8080/ping
```

**Expected response**:
```json
{
  "message": "pong"
}
```

✅ **Success!** The API is running.

---

## First Workflows

### Workflow 1: Login and Get Token

To access protected endpoints, you need to authenticate and obtain a bearer token. First, ensure you have a user in the system (you may need to create one via a database script or admin tool). Then login:

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Expected response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

**Save the token** - you'll need it for subsequent requests!

---

### Workflow 2: Create a User (Authenticated)

With your token, you can create new users:

```bash
TOKEN="<your-token-from-login>"

curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "jdoe",
    "name": "John Doe",
    "password": "password123",
    "emailAddress": "jdoe@example.com"
  }'
```

**Expected response** (201 Created):
```json
{
  "id": "987fcdeb-51a2-43e7-b456-426614174999",
  "username": "jdoe",
  "name": "John Doe",
  "emailAddress": "jdoe@example.com",
  "roles": []
}
```

---

### Workflow 4: Get User by ID

Retrieve a specific user:

```bash
USER_ID="987fcdeb-51a2-43e7-b456-426614174999"

curl http://localhost:8080/users/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected response** (200 OK):
```json
{
  "id": "987fcdeb-51a2-43e7-b456-426614174999",
  "username": "jdoe",
  "name": "John Doe",
  "emailAddress": "jdoe@example.com",
  "roles": []
}
```

---

### Workflow 4: Assign a Role

Assign the USER role to a user:

```bash
curl -X PUT http://localhost:8080/users/$USER_ID/roles/USER \
  -H "Authorization: Bearer $TOKEN"
```

**Expected response** (204 No Content)

Now retrieve the user again to see the role:

```bash
curl http://localhost:8080/users/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected response**:
```json
{
  "id": "987fcdeb-51a2-43e7-b456-426614174999",
  "username": "jdoe",
  "name": "John Doe",
  "emailAddress": "jdoe@example.com",
  "roles": [
    {
      "roleName": "USER",
      "permissions": ["users:read", "users:write"]
    }
  ]
}
```

---

### Workflow 6: List Users (Paginated)

List all users with pagination:

```bash
curl "http://localhost:8080/users?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected response** (200 OK):
```json
{
  "items": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "admin",
      "name": "Admin User",
      "emailAddress": "admin@example.com",
      "roles": []
    },
    {
      "id": "987fcdeb-51a2-43e7-b456-426614174999",
      "username": "jdoe",
      "name": "John Doe",
      "emailAddress": "jdoe@example.com",
      "roles": [
        {
          "roleName": "USER",
          "permissions": ["users:read", "users:write"]
        }
      ]
    }
  ],
  "page": 1,
  "pageSize": 10,
  "totalCount": 2,
  "totalPages": 1
}
```

---

### Workflow 7: Update a User

Update user information:

```bash
curl -X PUT http://localhost:8080/users/$USER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "John Q. Doe",
    "emailAddress": "john.doe@example.com"
  }'
```

**Expected response** (200 OK):
```json
{
  "id": "987fcdeb-51a2-43e7-b456-426614174999",
  "username": "jdoe",
  "name": "John Q. Doe",
  "emailAddress": "john.doe@example.com",
  "roles": [...]
}
```

**Note**: Partial updates allowed - only include fields you want to change.

---

### Workflow 8: Delete a User

Delete a user:

```bash
curl -X DELETE http://localhost:8080/users/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected response** (204 No Content)

Verify deletion:

```bash
curl http://localhost:8080/users/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected response** (404 Not Found):
```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "User not found"
}
```

---

## Common Scenarios

### Scenario: Invalid Credentials

Attempt login with wrong password:

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "wrongpassword"
  }'
```

**Expected response** (400 Bad Request):
```json
{
  "code": "AUTHENTICATION_FAILED",
  "message": "Invalid credentials"
}
```

**Note**: Same error for unknown username (no username enumeration).

---

### Scenario: Missing Authentication

Try to create a user without authentication token:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "name": "Test User",
    "password": "test123",
    "emailAddress": "test@example.com"
  }'
```

**Expected response** (401 Unauthorized):
```json
{
  "code": "AUTHENTICATION_REQUIRED",
  "message": "Authentication required"
}
```

---

### Scenario: Duplicate Email

Try to create user with existing email:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "another",
    "name": "Another User",
    "password": "password123",
    "emailAddress": "admin@example.com"
  }'
```

**Expected response** (409 Conflict):
```json
{
  "code": "CONFLICT",
  "message": "Email address already exists"
}
```

---

### Scenario: Feature Flag Disabled

If feature flag is disabled:

```bash
curl http://localhost:8080/users \
  -H "Authorization: Bearer $TOKEN"
```

**Expected response** (404 Not Found):
```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Resource not found"
}
```

**Note**: Error does not reveal that feature exists but is disabled.

---

## Configuration Options

### Application Configuration

Edit `src/main/resources/application.yml`:

```yaml
# Feature Flags
FeatureFlag:
  usersApi: true  # Enable/disable users API

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:default-secret-change-in-production}
  expirationMs: 86400000  # 24 hours

# Database (H2)
spring:
  datasource:
    url: jdbc:h2:mem:specdriven;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
  h2:
    console:
      enabled: true
      path: /h2-console

# Server
server:
  port: 8080
```

### Environment Variables

Override configuration via environment:

```bash
# Enable feature flag
export FEATURE_FLAG_USERS_API=true

# Set JWT secret (production)
export JWT_SECRET=your-256-bit-secret-key-here

# Change server port
export SERVER_PORT=9090
```

---

## Testing the API

### Run All Tests

```bash
./gradlew test
```

**Expected output**:
```
BUILD SUCCESSFUL in 10s
15 tests passed
```

### Run Specific Test Class

```bash
./gradlew test --tests UserCrudIntegrationTest
```

### View Test Reports

```bash
# Open in browser
open build/reports/tests/test/index.html
```

---

## Database Access (H2 Console)

The H2 console is enabled for debugging:

1. Start the application
2. Open browser: **http://localhost:8080/h2-console**
3. Enter connection details:
   - **JDBC URL**: `jdbc:h2:mem:specdriven`
   - **Username**: `sa`
   - **Password**: *(leave empty)*
4. Click **Connect**

You can now run SQL queries directly:

```sql
-- View all users
SELECT * FROM users;

-- View role assignments
SELECT u.username, r.role_name 
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;

-- View permissions for a role
SELECT r.role_name, p.permission
FROM roles r
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.role_name = 'ADMIN';
```

---

## Troubleshooting

### Problem: Build fails with "OpenAPI spec not found"

**Solution**: Ensure `src/main/resources/openapi.yaml` exists. If missing, check the contracts directory.

### Problem: Application fails to start

**Solution**: Check if port 8080 is already in use:
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

Change port in `application.yml` or use environment variable:
```bash
export SERVER_PORT=9090
./gradlew bootRun
```

### Problem: Feature flag always disabled

**Solution**: Ensure you've either:
1. Edited `application.yml` and set `FeatureFlag.usersApi: true`, OR
2. Set environment variable: `FEATURE_FLAG_USERS_API=true`, OR
3. Used command-line arg: `--FeatureFlag.usersApi=true`

### Problem: Token expired

**Solution**: Login again to get a fresh token. Default expiration is 24 hours.

### Problem: Test failures after code changes

**Solution**: 
1. Rebuild: `./gradlew clean build`
2. Check test output: `build/reports/tests/test/index.html`
3. Ensure OpenAPI contract matches implementation

---

## API Reference Summary

### Authentication

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/login` | POST | No | Login and get bearer token |

### Users

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/users` | POST | Yes | Create user (authentication required) |
| `/users` | GET | Yes | List users (paginated) |
| `/users/{id}` | GET | Yes | Get user by ID |
| `/users/{id}` | PUT | Yes | Update user |
| `/users/{id}` | DELETE | Yes | Delete user |


### Roles

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/users/{id}/roles/{roleName}` | PUT | Yes | Assign role to user |
| `/users/{id}/roles/{roleName}` | DELETE | Yes | Remove role from user |

### Health

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/ping` | GET | No | Health check |

---

## Next Steps

Now that you're up and running:

1. **Explore the OpenAPI contract**: `src/main/resources/openapi.yaml`
2. **Read the implementation plan**: `specs/main/plan.md`
3. **Review the data model**: `specs/main/data-model.md`
4. **Run integration tests**: `./gradlew test`
5. **Deploy to production**: Build JAR and configure environment variables

---

## Additional Resources

- **OpenAPI Specification**: `/src/main/resources/openapi.yaml`
- **Implementation Plan**: `/specs/main/plan.md`
- **Data Model**: `/specs/main/data-model.md`
- **Research Decisions**: `/specs/main/research.md`
- **Constitution**: `/.specify/memory/constitution.md`

---

**Quickstart Status**: ✅ Complete  
**Last Updated**: 2025-12-29  
**Questions?** Check the [spec.md](./spec.md) for detailed requirements.

