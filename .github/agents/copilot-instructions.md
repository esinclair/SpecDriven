﻿# SpecDriven Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-12-26

## Active Technologies
- Java 17 (Gradle toolchains; Spring Boot 3) + Spring Boot 3 (Web, Validation), OpenAPI Generator Gradle plugin 7.14.0 (002-api-contract-reliability)
- N/A (current API is stateless `/ping`) (002-api-contract-reliability)
- Java 17 (Gradle toolchain) + Spring Boot 3.5.x, Spring Web, Spring Validation, Spring Security, OpenAPI Generator (003-users-api)
- SQL-compatible DB via Spring datasource properties; local-dev default H2 in-memory (003-users-api)

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
- 003-users-api: Added Java 17 (Gradle toolchain) + Spring Boot 3.5.x, Spring Web, Spring Validation, Spring Security, OpenAPI Generator
- 003-users-api: Added Java 17 (Gradle toolchain) + Spring Boot 3.5.x, Spring Web, Spring Validation, Spring Security, OpenAPI Generator
- 002-api-contract-reliability: Added Java 17 (Gradle toolchains; Spring Boot 3) + Spring Boot 3 (Web, Validation), OpenAPI Generator Gradle plugin 7.14.0

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
