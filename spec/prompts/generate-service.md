# Generate ItemService

Context:

- Domain model: spec/domain.yaml
- API contract: spec/api.yaml

Instructions for GitHub Copilot (use when requesting code generation):

1. Produce a single Java Spring Boot service class named `ItemService` in package `com.example.service` that supports creating an Item from a DTO and returns the created entity with id populated.
2. Add input validation: `name` non-empty, `price` >= 0.
3. Keep implementation minimal but compile-safe — use in-memory storage (ConcurrentHashMap) and UUID generation.
4. Include method signatures and Javadoc comments. Do not include unrelated files.

Example prompt usage:

"Using the domain model (attached) and the API contract (attached), generate a Java Spring service class `ItemService` (package com.example.service) that provides a `create(ItemDto dto)` method. Use java.util.UUID for ids and validate inputs. Return a DTO with id. Show only the Java class content."
