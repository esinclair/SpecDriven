# SpecDriven Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-12-26

## Active Technologies
- Java 17 (Gradle toolchains; Spring Boot 3) + Spring Boot 3 (Web, Validation), OpenAPI Generator Gradle plugin 7.14.0 (002-api-contract-reliability)
- N/A (current API is stateless `/ping`) (002-api-contract-reliability)
- Java 17 (Gradle toolchain) + Spring Boot 3.5.x, Spring Web, Spring Validation, Spring Security, OpenAPI Generator (003-users-api)
- SQL-compatible DB via Spring datasource properties; local-dev default H2 in-memory (003-users-api)
- Java 17 (Gradle toolchain) + Spring Boot 3.5.9, Spring Web, Spring Validation, Spring Data JDBC, Spring Security, OpenAPI Generator 7.14.0, Flyway, JWT (io.jsonwebtoken 0.12.3) (main)
- H2 in-memory database (local dev) with PostgreSQL compatibility mode; production supports any SQL database via Spring JDBC (main)
- Java 17 (JDK 17 with Spring Boot 3.5.9) + Spring Boot 3.5.9, Spring Web, Spring Data JDBC, Spring Security, Spring Validation, JWT (jjwt 0.12.3), Flyway, OpenAPI Generator 7.14.0 (main)
- H2 in-memory database (SQL-compliant mode), Spring Data JDBC repositories (main)
- [e.g., Java 21 or NEEDS CLARIFICATION] + [e.g., Spring Boot, Spring Web, Validation or NEEDS CLARIFICATION] (copilot/add-speckit-plan)
- [if applicable, e.g., PostgreSQL, files or N/A] (copilot/add-speckit-plan)
- Java 17 (JDK 17 with Spring Boot 3.5.9) + Spring Boot 3.5.9, Spring Web, Spring Data JDBC, Spring Security, Spring Validation, JWT (jjwt 0.12.6), Flyway, OpenAPI Generator 7.10.0 (copilot/add-speckit-plan)

## Project Structure

```text
src/main/java
src/main/resources
src/main-gen/java   # generated, gitignored (OpenAPI)
src/test/java
src/test-gen/java   # reserved for generated tests, gitignored
specs/
```

## Commands

```text
./gradlew test
./gradlew clean
```

## Code Style

Java: Follow standard Java + Spring Boot conventions.

## Recent Changes
- copilot/add-speckit-plan: Added Java 17 (JDK 17 with Spring Boot 3.5.9) + Spring Boot 3.5.9, Spring Web, Spring Data JDBC, Spring Security, Spring Validation, JWT (jjwt 0.12.6), Flyway, OpenAPI Generator 7.10.0
- copilot/add-speckit-plan: Added [e.g., Java 21 or NEEDS CLARIFICATION] + [e.g., Spring Boot, Spring Web, Validation or NEEDS CLARIFICATION]
- main: Added Java 17 (JDK 17 with Spring Boot 3.5.9) + Spring Boot 3.5.9, Spring Web, Spring Data JDBC, Spring Security, Spring Validation, JWT (jjwt 0.12.3), Flyway, OpenAPI Generator 7.14.0

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
