# QuickStart Guide: SpecDriven API System

**Feature**: Overall System Architecture  
**Branch**: `main`  
**Date**: 2025-12-27  
**Last Updated**: 2025-12-27

## Purpose

This guide helps developers quickly set up, build, test, and run the SpecDriven API system locally.

---

## Prerequisites

- **Java 17 or later** (JDK with toolchain support)
- **Git** for cloning the repository
- **IDE** (IntelliJ IDEA, VS Code, Eclipse) - optional but recommended

**Note**: Gradle wrapper is included, so you don't need to install Gradle separately.

---

## Quick Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd SpecDriven
```

### 2. Build the Project

On Windows (PowerShell or CMD):
```powershell
.\gradlew.bat clean build
```

On Linux/Mac:
```bash
./gradlew clean build
```

This will:
- Run OpenAPI Generator to create interfaces and DTOs
- Compile Java sources (main + generated)
- Run Flyway migrations on test database
- Execute all tests (unit + integration)

**Expected output**: `BUILD SUCCESSFUL`

### 3. Run the Application

```powershell
.\gradlew.bat bootRun
```

The application starts on **http://localhost:8080**

**Verify it's running**:
```bash
curl http://localhost:8080/ping
# Expected: {"message":"pong"}
```

---

## Feature Flags

By default, the Users API feature is **disabled**. To enable it:

### Option 1: Edit application.properties

Edit `src/main/resources/application.properties`:
```properties
feature-flag.users-api=true
```

### Option 2: Environment Variable (no code changes)

On Windows:
```powershell
$env:FEATURE_FLAG_USERS_API="true"
.\gradlew.bat bootRun
```

On Linux/Mac:
```bash
export FEATURE_FLAG_USERS_API=true
./gradlew bootRun
```

### Option 3: Command-line Argument

```powershell
.\gradlew.bat bootRun --args='--feature-flag.users-api=true'
```

---

## First User (Bootstrap Mode)

When the Users API feature is enabled and there are **zero users** in the database, you can create the first user **without authentication**:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "name": "System Administrator",
    "password": "SecurePass123",
    "emailAddress": "admin@example.com"
  }'
```

**Response**: `201 Created` with the user object

**Important**: After the first user is created, `POST /users` requires authentication.

---

## Authentication

### Login

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "SecurePass123"
  }'
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

Save the token for subsequent requests.

### Using the Token

Include the token in the `Authorization` header:

```bash
curl -X GET http://localhost:8080/users?page=1&pageSize=10 \
  -H "Authorization: Bearer <your-token-here>"
```

---

## Common API Operations

All examples assume you've logged in and have a token stored in `$TOKEN`.

### Health Check (No Auth Required)

```bash
curl http://localhost:8080/ping
```

### Create Additional Users (Auth Required)

```bash
curl -X POST http://localhost:8080/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "name": "John Doe",
    "password": "UserPass456",
    "emailAddress": "john.doe@example.com"
  }'
```

### Get User by ID

```bash
curl -X GET http://localhost:8080/users/<user-id> \
  -H "Authorization: Bearer $TOKEN"
```

### List Users (Paginated)

```bash
curl -X GET "http://localhost:8080/users?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"
```

### List Users with Filters

```bash
# Filter by username
curl -X GET "http://localhost:8080/users?page=1&pageSize=10&username=admin" \
  -H "Authorization: Bearer $TOKEN"

# Filter by role
curl -X GET "http://localhost:8080/users?page=1&pageSize=10&roleName=ADMIN" \
  -H "Authorization: Bearer $TOKEN"

# Filter by name (partial match)
curl -X GET "http://localhost:8080/users?page=1&pageSize=10&name=John" \
  -H "Authorization: Bearer $TOKEN"
```

### Update User

```bash
curl -X PUT http://localhost:8080/users/<user-id> \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith"
  }'
```

### Delete User

```bash
curl -X DELETE http://localhost:8080/users/<user-id> \
  -H "Authorization: Bearer $TOKEN"
```

### Assign Role to User

```bash
curl -X PUT http://localhost:8080/users/<user-id>/roles/ADMIN \
  -H "Authorization: Bearer $TOKEN"
```

### Remove Role from User

```bash
curl -X DELETE http://localhost:8080/users/<user-id>/roles/USER \
  -H "Authorization: Bearer $TOKEN"
```

---

## Development Workflow

### Modify the API Contract

1. **Edit the OpenAPI spec**: `src/main/resources/openapi.yaml`
2. **Regenerate code**: Run `.\gradlew.bat clean build`
3. **Implement changes**: Update controller implementations in `src/main/java`
4. **Write tests**: Add/update tests in `src/test/java`
5. **Verify**: Run `.\gradlew.bat test`

**Important**: Never manually edit files in `src/main-gen/`. They are auto-generated.

### Add a New Endpoint

1. Add the endpoint to `openapi.yaml`
2. Regenerate: `.\gradlew.bat openApiGenerate`
3. Implement the interface in a controller
4. Add tests
5. Run tests: `.\gradlew.bat test`

### Database Migrations

**Create a new migration**:

1. Create file: `src/main/resources/db/migration/V004__description.sql`
2. Write SQL (DDL or DML)
3. Restart application - Flyway runs automatically

**View migration status**:
- Check application logs on startup
- Flyway logs all applied migrations

---

## Testing

### Run All Tests

```powershell
.\gradlew.bat test
```

### Run Specific Test Class

```powershell
.\gradlew.bat test --tests "com.example.specdriven.ping.PingControllerTest"
```

### View Test Reports

After running tests, open:
```
build/reports/tests/test/index.html
```

### Integration Tests

Integration tests use Spring Boot Test with the same H2 in-memory database:
- Each test gets a clean database
- Flyway migrations run before tests
- All Spring beans are loaded

Example:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UsersApiIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    // Tests here
}
```

---

## Configuration

### Database Configuration

**Development** (default in `application.properties`):
```properties
spring.datasource.url=jdbc:h2:mem:specdriven;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
```

**Production** (override via environment variables):
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/specdriven
export SPRING_DATASOURCE_USERNAME=dbuser
export SPRING_DATASOURCE_PASSWORD=dbpass
```

### JWT Configuration

**Development** (default):
```properties
jwt.secret=change-me-in-production-use-at-least-256-bit-secret-key-for-hs256
jwt.expiration-ms=3600000
```

**Production** (MUST change the secret):
```bash
export JWT_SECRET=<your-secure-256-bit-secret>
export JWT_EXPIRATION_MS=3600000
```

### Flyway Configuration

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

---

## Troubleshooting

### Build Fails with "OpenAPI spec not found"

**Cause**: Missing or incorrect OpenAPI spec path  
**Solution**: Verify `src/main/resources/openapi.yaml` exists

### Tests Fail with "401 Unauthorized"

**Cause**: Missing or invalid JWT token in test  
**Solution**: Check test setup - authenticate first, then use token

### Feature Endpoint Returns 404

**Cause**: Feature flag is disabled  
**Solution**: Enable the feature flag (see Feature Flags section)

### "Validation failed" Errors

**Cause**: Request doesn't match OpenAPI schema  
**Solution**: Check request body against schema in `openapi.yaml`

### Database Migration Fails

**Cause**: Migration file has errors or conflicts  
**Solution**: 
- Check Flyway logs in application output
- Verify SQL syntax
- Never modify existing migrations - create new ones

### Token Expired

**Cause**: JWT token has expired (default: 1 hour)  
**Solution**: Login again to get a new token

---

## IDE Setup

### IntelliJ IDEA

1. **Import Project**: File → Open → Select `SpecDriven` folder
2. **Gradle Sync**: IntelliJ auto-detects Gradle and syncs
3. **Enable Annotation Processing**: 
   - Settings → Build → Compiler → Annotation Processors
   - Enable annotation processing
4. **Mark Generated Sources**:
   - Right-click `src/main-gen/java` → Mark Directory As → Generated Sources Root

### VS Code

1. **Install Extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Gradle for Java
2. **Open Folder**: File → Open Folder → Select `SpecDriven`
3. **Sync Gradle**: VS Code auto-detects Gradle

### Eclipse

1. **Import Project**: File → Import → Gradle → Existing Gradle Project
2. **Select Root Directory**: Choose `SpecDriven` folder
3. **Finish**: Eclipse imports and builds

---

## Production Deployment Checklist

- [ ] Change `jwt.secret` to a secure random value (≥256 bits)
- [ ] Update database configuration (PostgreSQL/MySQL)
- [ ] Enable production logging configuration
- [ ] Set feature flags appropriately
- [ ] Configure connection pooling
- [ ] Set up monitoring and health checks
- [ ] Review and test Flyway migrations against production-like data
- [ ] Configure HTTPS/TLS
- [ ] Set appropriate CORS policies
- [ ] Review security headers
- [ ] Test authentication/authorization flows
- [ ] Load test for performance budget (<1s response time)

---

## Useful Commands Reference

| Task | Command |
|------|---------|
| Build project | `.\gradlew.bat clean build` |
| Run application | `.\gradlew.bat bootRun` |
| Run tests | `.\gradlew.bat test` |
| Generate OpenAPI code | `.\gradlew.bat openApiGenerate` |
| Check dependencies | `.\gradlew.bat dependencies` |
| View tasks | `.\gradlew.bat tasks` |
| Create JAR | `.\gradlew.bat bootJar` |
| Run with profile | `.\gradlew.bat bootRun --args='--spring.profiles.active=prod'` |

---

## Additional Resources

- **OpenAPI Spec**: `src/main/resources/openapi.yaml`
- **Constitution**: `.specify/memory/constitution.md`
- **Feature Specs**: `specs/` directory
- **Implementation Plans**: `specs/*/plan.md`
- **Test Reports**: `build/reports/tests/test/index.html`

---

## Support & Contribution

### Reporting Issues

- Check existing issues first
- Include error logs and steps to reproduce
- Specify Java version and OS

### Contributing

1. Read the constitution: `.specify/memory/constitution.md`
2. Create a feature spec in `specs/`
3. Follow OpenAPI-first workflow
4. Write tests (happy path + negative scenarios)
5. Ensure `./gradlew test` passes
6. Submit PR with clear description

---

## Next Steps

1. ✅ **Start the application**: `.\gradlew.bat bootRun`
2. ✅ **Enable Users API**: Set `feature-flag.users-api=true`
3. ✅ **Create first user**: Use bootstrap mode (no auth)
4. ✅ **Login**: Get JWT token
5. ✅ **Explore API**: Try CRUD operations
6. ✅ **Review code**: Explore `src/main/java` structure
7. ✅ **Run tests**: `.\gradlew.bat test`
8. ✅ **Read specs**: Review `specs/` directory

**Happy coding!** 🚀

