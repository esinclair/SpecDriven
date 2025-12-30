# Quickstart Guide: User Management API System

**Feature**: User Management API System | **Date**: 2025-12-30 | **Spec**: [spec.md](./spec.md)

## Overview

This guide provides step-by-step instructions to build, run, and interact with the User Management API. The API is built using Spring Boot 3.5.9, JDK 17, and follows an OpenAPI-first development approach.

## Prerequisites

Before you begin, ensure you have the following installed:

- **JDK 17** (OpenJDK or Oracle JDK)
- **Git** (for cloning the repository)
- **curl** or **Postman** (for testing API endpoints)
- **Optional**: IDE with Java support (IntelliJ IDEA, Eclipse, VS Code with Java extensions)

## Quick Start (5 Minutes)

### 1. Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd SpecDriven

# Build the project (generates OpenAPI code, compiles, runs tests)
./gradlew clean build

# Expected output: BUILD SUCCESSFUL
# Build time: ~30-60 seconds on first run (downloads dependencies)
```

### 2. Run the Application

```bash
# Start the Spring Boot application
./gradlew bootRun

# Expected output:
# Started SpecDrivenApplication in X.XXX seconds
# Server is running on http://localhost:8080
```

The application will:
- Start an embedded Tomcat server on port 8080
- Initialize the H2 in-memory database
- Run Flyway migrations to create schema and seed roles
- Generate and load OpenAPI-generated API code

### 3. Verify Health

```bash
# Test the health check endpoint (no authentication required)
curl http://localhost:8080/ping

# Expected response (200 OK):
# {
#   "status": "UP",
#   "timestamp": "2025-12-30T15:30:00Z"
# }
```

**Note**: If the feature flag `FeatureFlag.usersApi` is disabled by default, you'll need to enable it first (see Configuration section below).

### 4. Enable Feature Flag (if needed)

If user endpoints return 404, enable the feature flag:

```bash
# Edit src/main/resources/application.properties
# Add or change:
FeatureFlag.usersApi=true

# Restart the application
# Press Ctrl+C to stop, then run ./gradlew bootRun again
```

### 5. Create First User (Bootstrap Mode)

```bash
# Create the first user (no authentication required when database is empty)
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "name": "System Administrator",
    "emailAddress": "admin@example.com",
    "password": "SecurePass123"
  }'

# Expected response (201 Created):
# {
#   "id": "550e8400-e29b-41d4-a716-446655440000",
#   "username": "admin",
#   "name": "System Administrator",
#   "emailAddress": "admin@example.com",
#   "roles": [],
#   "createdAt": "2025-12-30T15:30:00Z",
#   "updatedAt": "2025-12-30T15:30:00Z"
# }

# Save the user ID for later use
USER_ID="550e8400-e29b-41d4-a716-446655440000"
```

### 6. Authenticate and Get Token

```bash
# Login with the created user credentials
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "SecurePass123"
  }'

# Expected response (200 OK):
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "tokenType": "Bearer",
#   "expiresIn": 86400
# }

# Save the token for subsequent requests
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 7. Use Protected Endpoints

```bash
# Retrieve the user (requires authentication)
curl http://localhost:8080/users/$USER_ID \
  -H "Authorization: Bearer $TOKEN"

# Expected response (200 OK):
# {
#   "id": "550e8400-e29b-41d4-a716-446655440000",
#   "username": "admin",
#   "name": "System Administrator",
#   "emailAddress": "admin@example.com",
#   "roles": [],
#   "createdAt": "2025-12-30T15:30:00Z",
#   "updatedAt": "2025-12-30T15:30:00Z"
# }
```

## API Workflow Examples

### Complete User Management Workflow

```bash
# 1. Create a user
curl -X POST http://localhost:8080/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "name": "John Doe",
    "emailAddress": "john.doe@example.com",
    "password": "Password123"
  }'

# Save the returned user ID
NEW_USER_ID="<id-from-response>"

# 2. Assign a role to the user
curl -X POST http://localhost:8080/users/$NEW_USER_ID/roles/USER \
  -H "Authorization: Bearer $TOKEN"

# Expected: 204 No Content

# 3. Retrieve the user to verify role assignment
curl http://localhost:8080/users/$NEW_USER_ID \
  -H "Authorization: Bearer $TOKEN"

# Expected: User object with "roles": ["USER"]

# 4. Update the user
curl -X PUT http://localhost:8080/users/$NEW_USER_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John A. Doe",
    "emailAddress": "john.doe@example.com"
  }'

# Expected: 200 OK with updated user

# 5. List users with pagination
curl "http://localhost:8080/users?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK with paged result

# 6. Remove role from user
curl -X DELETE http://localhost:8080/users/$NEW_USER_ID/roles/USER \
  -H "Authorization: Bearer $TOKEN"

# Expected: 204 No Content

# 7. Delete the user
curl -X DELETE http://localhost:8080/users/$NEW_USER_ID \
  -H "Authorization: Bearer $TOKEN"

# Expected: 204 No Content
```

### Pagination Example

```bash
# List users with pagination and filtering
curl "http://localhost:8080/users?page=1&pageSize=20&username=john" \
  -H "Authorization: Bearer $TOKEN"

# Expected response:
# {
#   "items": [
#     { "id": "...", "username": "john.doe", ... },
#     { "id": "...", "username": "johnny", ... }
#   ],
#   "page": 1,
#   "pageSize": 20,
#   "totalCount": 2,
#   "totalPages": 1
# }

# Get next page
curl "http://localhost:8080/users?page=2&pageSize=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Role Management Example

```bash
# Assign multiple roles to a user
curl -X POST http://localhost:8080/users/$USER_ID/roles/ADMIN \
  -H "Authorization: Bearer $TOKEN"

curl -X POST http://localhost:8080/users/$USER_ID/roles/USER \
  -H "Authorization: Bearer $TOKEN"

# Retrieve user to see all assigned roles
curl http://localhost:8080/users/$USER_ID \
  -H "Authorization: Bearer $TOKEN"

# Expected: "roles": ["ADMIN", "USER"]

# Remove a specific role
curl -X DELETE http://localhost:8080/users/$USER_ID/roles/USER \
  -H "Authorization: Bearer $TOKEN"

# Verify removal
curl http://localhost:8080/users/$USER_ID \
  -H "Authorization: Bearer $TOKEN"

# Expected: "roles": ["ADMIN"]
```

## Error Handling Examples

### Validation Errors (400)

```bash
# Create user with invalid email
curl -X POST http://localhost:8080/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "name": "Test User",
    "emailAddress": "invalid-email",
    "password": "short"
  }'

# Expected response (400 Bad Request):
# {
#   "code": "VALIDATION_FAILED",
#   "message": "Validation failed",
#   "details": {
#     "emailAddress": "must be a well-formed email address",
#     "password": "size must be between 8 and 255"
#   }
# }
```

### Authentication Errors (401)

```bash
# Access protected endpoint without token
curl http://localhost:8080/users/$USER_ID

# Expected response (401 Unauthorized):
# {
#   "code": "AUTHENTICATION_REQUIRED",
#   "message": "Authentication required"
# }

# Use expired or invalid token
curl http://localhost:8080/users/$USER_ID \
  -H "Authorization: Bearer invalid-token"

# Expected response (401 Unauthorized):
# {
#   "code": "AUTHENTICATION_FAILED",
#   "message": "Invalid or expired token"
# }
```

### Not Found Errors (404)

```bash
# Retrieve non-existent user
curl http://localhost:8080/users/00000000-0000-0000-0000-000000000000 \
  -H "Authorization: Bearer $TOKEN"

# Expected response (404 Not Found):
# {
#   "code": "RESOURCE_NOT_FOUND",
#   "message": "User not found"
# }
```

### Conflict Errors (409)

```bash
# Create user with duplicate email
curl -X POST http://localhost:8080/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "duplicate",
    "name": "Duplicate User",
    "emailAddress": "admin@example.com",
    "password": "Password123"
  }'

# Expected response (409 Conflict):
# {
#   "code": "CONFLICT",
#   "message": "Email address already exists"
# }
```

## Configuration

### Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Feature Flags
FeatureFlag.usersApi=true

# Database Configuration (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:usermgmt;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (Development only - disable in production)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# JWT Configuration
jwt.secret=your-secret-key-here-change-in-production
jwt.expiration=86400

# Logging
logging.level.root=INFO
logging.level.com.specdriven=DEBUG
```

### Environment Variables

You can override properties using environment variables:

```bash
# Set feature flag
export FEATUREFLAG_USERSAPI=true

# Set JWT secret
export JWT_SECRET=my-production-secret-key

# Set server port
export SERVER_PORT=9090

# Run application
./gradlew bootRun
```

### Profile-Specific Configuration

Create `application-dev.properties` for development:

```properties
# Development-specific settings
spring.h2.console.enabled=true
logging.level.com.specdriven=DEBUG
FeatureFlag.usersApi=true
```

Create `application-prod.properties` for production:

```properties
# Production-specific settings
spring.h2.console.enabled=false
logging.level.com.specdriven=INFO
jwt.secret=${JWT_SECRET}
```

Run with profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Development Workflow

### 1. Modify OpenAPI Contract

```bash
# Edit the OpenAPI specification
vi specs/copilot/add-speckit-plan/contracts/user-management-api.yaml

# Regenerate code and rebuild
./gradlew clean build

# Implement changes in controller classes
vi src/main/java/com/specdriven/api/UsersApiController.java
```

### 2. Run Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests UserServiceTest

# Run integration tests only
./gradlew integrationTest

# Run with coverage report
./gradlew test jacocoTestReport
# View report: build/reports/jacoco/test/html/index.html
```

### 3. Database Console

Access H2 console for debugging (development only):

```bash
# 1. Start application: ./gradlew bootRun
# 2. Open browser: http://localhost:8080/h2-console
# 3. JDBC URL: jdbc:h2:mem:usermgmt
# 4. Username: sa
# 5. Password: (leave blank)
```

### 4. API Documentation

View OpenAPI documentation:

```bash
# OpenAPI spec is available at:
# File: specs/copilot/add-speckit-plan/contracts/user-management-api.yaml

# You can use tools like Swagger UI or Redoc to visualize:
# - Swagger UI: https://editor.swagger.io/ (paste YAML content)
# - VS Code: OpenAPI (Swagger) Editor extension
```

## Testing

### Manual Testing with curl

Use the provided examples above with curl.

### Automated Testing

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests with coverage
./gradlew clean test jacocoTestReport
```

### Using Postman

1. Import the OpenAPI spec into Postman:
   - File → Import → Upload `contracts/user-management-api.yaml`
2. Postman will create a collection with all endpoints
3. Configure environment variables:
   - `baseUrl`: http://localhost:8080
   - `token`: (obtained from /login response)
4. Use Pre-request Scripts to set Authorization header:
   ```javascript
   pm.request.headers.add({
     key: 'Authorization',
     value: 'Bearer ' + pm.environment.get('token')
   });
   ```

## Troubleshooting

### Port Already in Use

```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process or change port
export SERVER_PORT=9090
./gradlew bootRun
```

### Build Failures

```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# Check Java version
java -version  # Should be 17

# Check Gradle version
./gradlew --version
```

### Database Issues

```bash
# H2 in-memory database is recreated on each restart
# If migrations fail, check:
ls -la src/main/resources/db/migration/

# Ensure migration files follow naming convention:
# V001__description.sql, V002__description.sql, etc.
```

### Authentication Issues

```bash
# Token expired - get a new token
curl -X POST http://localhost:8080/login ...

# Token format incorrect - ensure "Bearer " prefix
# Correct: Authorization: Bearer eyJhbGci...
# Wrong:   Authorization: eyJhbGci...
```

### Feature Flag Disabled

```bash
# If endpoints return 404, check feature flag
# Edit src/main/resources/application.properties:
FeatureFlag.usersApi=true

# Or set environment variable:
export FEATUREFLAG_USERSAPI=true
```

## Performance Testing

### Basic Load Test

```bash
# Install Apache Bench (ab) or similar tool
# Ubuntu: sudo apt-get install apache2-utils

# Test health endpoint (should be < 1s for 95th percentile)
ab -n 1000 -c 10 http://localhost:8080/ping

# Test authenticated endpoint
# First, get token and save to file
echo "Bearer $TOKEN" > token.txt

# Use with ab (requires custom script or tool like wrk)
```

### Performance Budget Verification

Constitution requires ≤1s for 95% of requests:

- Health check: < 1s
- Login: < 1s
- User CRUD: < 1s
- List (paginated): < 1s

Monitor response times during testing and optimize if needed.

## Next Steps

1. **Explore the API**: Try all endpoints using the examples above
2. **Review the Code**: Check the implementation in `src/main/java`
3. **Run Tests**: Execute `./gradlew test` to see test coverage
4. **Modify OpenAPI**: Make changes to the contract and regenerate code
5. **Add Features**: Extend the API while maintaining backward compatibility

## Additional Resources

- **Spec**: [spec.md](./spec.md) - Full feature specification
- **Plan**: [plan.md](./plan.md) - Implementation plan
- **Data Model**: [data-model.md](./data-model.md) - Entity and relationship details
- **Research**: [research.md](./research.md) - Technology decisions and rationale
- **OpenAPI Spec**: [contracts/user-management-api.yaml](./contracts/user-management-api.yaml) - API contract
- **Constitution**: `/.specify/memory/constitution.md` - Project principles and standards

## Support

For issues or questions:
- Check the spec and plan documents
- Review error messages and logs
- Consult the constitution for project standards
- Review test cases for usage examples
