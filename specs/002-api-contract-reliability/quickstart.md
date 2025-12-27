# Quickstart (Feature 002): OpenAPI Contract & Reliability Standards

This repo is **OpenAPI-first**. The contract in `src/main/resources/openapi.yaml` is the diff-of-record, and Java interfaces/DTOs are generated into `src/main-gen/java` (gitignored).

## Key conventions

- Contract source of truth: `src/main/resources/openapi.yaml`
- Generated server stubs:
  - Main: `src/main-gen/java` (wired into Gradle `sourceSets.main`)
  - Reserved test-gen: `src/test-gen/java` (wired into `sourceSets.test`)
- Do **not** manually edit generated files. Change the OpenAPI spec and regenerate.
- Public API is **unversioned**: no `/v1`, no version headers/query params/media types.

## Typical workflow

1) Edit `src/main/resources/openapi.yaml` first.
2) Build/tests run generation automatically (Gradle wires `openApiGenerate` into compile/resources).
3) Implement handwritten controllers/services under `src/main/java` implementing the generated interfaces.
4) Add tests under `src/test/java`:
   - at least one happy-path test
   - mandatory negative tests: invalid input (when applicable) + error responses with `ErrorResponse`

## Generated directories and git

- `src/main-gen/` and `src/test-gen/` are gitignored.
- `clean` deletes both generated directories.

### If we ever need a placeholder for `src/test-gen/`
Git doesn’t track empty directories. If we later decide that `src/test-gen/` must exist in git (e.g., IDE convenience), create `src/test-gen/.gitkeep` and add an exception to `.gitignore`:

- keep ignoring: `src/test-gen/`
- add: `!src/test-gen/.gitkeep`

## Deterministic generation checklist

- Only one input spec: `src/main/resources/openapi.yaml`
- Pin generator version (already pinned via Gradle plugin `org.openapi.generator`) and avoid “latest” dependencies.
- Avoid generator options that inject timestamps; if encountered, disable them.
- Ensure generation happens before compilation (already enforced by Gradle task dependencies).

## IntelliJ IDEA tips

- Import the project via Gradle (recommended).
- After the first build, IntelliJ should mark `src/main-gen/java` as a source root because it’s in Gradle `sourceSets`. If it doesn’t, reimport the Gradle project.

