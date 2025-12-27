# Phase 0 Research: OpenAPI Contract & Reliability Standards

This document resolves all items marked **NEEDS CLARIFICATION** in `plan.md` and records key technical decisions needed to execute the feature.

## 1) Target platform / runtime expectations

### Decision
Target platform is a **generic JVM runtime** (developer machines + CI) with **no OS-specific dependencies**; the deliverable is a Spring Boot application runnable via Gradle.

### Rationale
- The repository already uses Gradle wrapper and Spring Boot; keeping the runtime generic maximizes portability.
- OpenAPI generation is already wired into the Gradle lifecycle, so build + test on CI is the primary enforcement mechanism.

### Alternatives considered
- Docker-first deployment definition: useful later, but not required to satisfy the contract/reliability requirements.
- Linux-only assumptions: unnecessary constraint.

## 2) Code generation layout and determinism

### Decision
Keep generated outputs in **non-versioned** directories under repo root:
- Main generated sources: `src/main-gen/java`
- Reserved test generated sources: `src/test-gen/java`

Enforce determinism by:
- Treating `src/main/resources/openapi.yaml` as the **single input**
- Ensuring generation runs before compilation/resource processing
- Avoiding timestamps/unstable metadata in generated files where possible

### Rationale
- Aligns with the current build (`sourceFolder` override) and `.gitignore`.
- Makes it obvious which code is generated vs handwritten.

### Alternatives considered
- Generate to `build/generated/...`: cleaner from Gradle’s perspective, but conflicts with the repo’s current convention and IDE source root expectations.
- Check in generated sources: violates OpenAPI-first workflow and complicates drift control.

## 3) Handling src/test-gen and empty directory behavior

### Decision
Prefer **not** committing empty generated directories. If a future workflow requires `src/test-gen/` to exist in git (e.g., for IDE convenience), use a **`.gitkeep`** (or `.gitignore` placeholder) and update root `.gitignore` to allow only that file:
- Keep ignoring `src/test-gen/**`
- Add exception: `!src/test-gen/.gitkeep`

### Rationale
- `clean` deletes the generated dirs, so empty dirs don’t survive anyway.
- Git doesn’t track empty directories; a placeholder is the usual approach.

### Alternatives considered
- Always recreate the directory in a build step: possible, but adds noise and doesn’t solve git semantics.

## 4) IDE source roots (IntelliJ) with generated sources

### Decision
Rely primarily on the Gradle project model:
- `sourceSets.main.java.srcDir("src/main-gen/java")` is the source of truth

Optionally (future hardening):
- Add a lightweight IntelliJ recommendation in `quickstart.md` to reimport Gradle project after generation.

### Rationale
- IntelliJ generally recognizes Gradle source sets as source roots.
- Avoids committing IDE-specific configuration files.

### Alternatives considered
- Checking in `.idea` or `.iml` tweaks: not portable across environments.

## 5) Error contract and retry semantics

### Decision
Use a shared OpenAPI schema `ErrorResponse` with required fields:
- `code` (stable machine-readable)
- `message` (human-readable)
- optional `details` (object)

Retry guidance is expressed only via:
- HTTP status classes (4xx non-retryable by default; 5xx retryable by default)
- Standard headers (e.g., `Retry-After` for `503`/`429` when relevant)

### Rationale
Matches the feature spec + constitution and keeps the API interoperable.

### Alternatives considered
- Adding `retryable` boolean: explicitly forbidden.
- Non-standard headers: unnecessary.

## 6) Contract completeness for endpoints

### Decision
Even for `/ping`, document at least:
- `200` success
- `500` unexpected error with `ErrorResponse`

As the API grows, each operation will document relevant 4xx/5xx responses (validation, not found, conflict, rate limit, transient).

### Rationale
Prevents contract drift and ensures consumers can build reliable clients.

### Alternatives considered
- Only documenting 2xx responses: insufficient per spec (FR-012/FR-016).


