# Feature Specification: Stable API Error Codes

**Feature Branch**: `001-api-error-codes`  
**Created**: 2025-12-26  
**Status**: Draft  
**Input**: User description: "Run the speckit.specify workflow for this repo. Produce/update a feature specification (spec.md) consistent with the current project constitution: OpenAPI-first REST public API with implementation generated from OpenAPI, no `retryable` field (retry semantics via HTTP status codes + standard headers), <1s response goal, clear stable error codes in ErrorResponse, tests cover happy and failure flows. Place outputs in the repo's expected specs directory structure (e.g., /specs/<feature>/spec.md) and keep it aligned with the existing minimal sample endpoint (/v1/ping) and error handling approach."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Client receives stable error response (Priority: P1)

As an API client developer, I want all error responses to include a stable, documented error code and a safe human-readable message so that my client can reliably handle failures without parsing free-form text.

**Why this priority**: This is the foundation of a public API contract; without stable error codes, clients become brittle and support burden increases.

**Independent Test**: Can be fully tested by making requests that trigger a validation error and a server error and verifying the returned HTTP status and error body shape.

**Acceptance Scenarios**:

1. **Given** a request that fails input validation, **When** the server responds, **Then** the response status is 400 and the body matches the standard ErrorResponse schema with a stable `code` and safe `message`.
2. **Given** an unexpected server failure while handling a request, **When** the server responds, **Then** the response status is 500 and the body matches the standard ErrorResponse schema with an internal error `code`, and the `message` does not reveal sensitive details.

---

### User Story 2 - Client can reason about retry behavior via HTTP semantics (Priority: P2)

As an API client developer, I want retry guidance to be conveyed through HTTP status codes and standard headers so that I can implement correct retry/backoff behavior without relying on non-standard fields.

**Why this priority**: This keeps the API contract idiomatic and interoperable; retry behavior is a cross-cutting concern and must be consistent.

**Independent Test**: Can be tested by triggering a rate limit response and verifying the HTTP status and presence/absence of expected standard headers.

**Acceptance Scenarios**:

1. **Given** the client is rate limited, **When** the server responds, **Then** it uses an appropriate 4xx status and includes standard retry guidance headers when a retry time is known.
2. **Given** a temporary dependency outage affects a request, **When** the server responds, **Then** it uses an appropriate 5xx status and does not include any non-standard `retryable` field in the response body.

---

### User Story 3 - API maintainers extend errors safely (Priority: P3)

As an API maintainer, I want a predictable process for introducing new error codes and documenting them so that future endpoints can adopt the same error contract without breaking existing clients.

**Why this priority**: This enables safe evolution of the API and prevents accidental breaking changes caused by ad-hoc error handling.

**Independent Test**: Can be tested by introducing a new error code in documentation and verifying existing codes remain unchanged and previously documented client behavior still works.

**Acceptance Scenarios**:

1. **Given** an existing documented error code, **When** new codes are added later, **Then** the existing code identifiers and meanings remain unchanged.
2. **Given** a new error condition is introduced, **When** it is exposed to clients, **Then** it uses a newly documented `code` and continues to conform to the standard ErrorResponse schema.

---

### Edge Cases

- Unknown/unhandled exceptions: ensure response still conforms to ErrorResponse and uses the internal error code.
- Invalid/missing required fields: ensure 400 responses consistently use the validation error code.
- Requests to non-existent routes/resources under `/v1/*`: ensure a consistent not-found error code is returned.
- Content negotiation failures (unsupported `Content-Type` or unacceptable `Accept`): ensure a consistent, documented error response.
- Large or malformed inputs: ensure failure responses remain fast and do not reflect sensitive input back to clients.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The API MUST define a single, shared ErrorResponse schema for all error responses across `/v1/*` endpoints.
- **FR-002**: ErrorResponse MUST contain a stable `code` (string) and a safe human-readable `message`.
- **FR-003**: ErrorResponse MAY contain `details` as an optional structured object for diagnostics; clients MUST be able to ignore it without losing the ability to handle the error.
- **FR-004**: The API MUST NOT include a `retryable` (or equivalent) boolean field in ErrorResponse.
- **FR-005**: The API MUST document a stable set of error codes and their meanings, and those codes MUST be treated as part of the public contract (no renames or semantic changes).
- **FR-006**: For validation failures, the API MUST respond with HTTP 400 and an ErrorResponse whose `code` indicates validation failure.
- **FR-007**: For unknown/unhandled server errors, the API MUST respond with HTTP 500 and an ErrorResponse whose `code` indicates internal error.
- **FR-008**: For requests to non-existent resources/endpoints under `/v1/*`, the API MUST respond with HTTP 404 and an ErrorResponse whose `code` indicates resource not found.
- **FR-009**: For rate limiting, the API MUST respond with an appropriate HTTP status code and MUST use standard HTTP headers to convey retry timing when a retry time is known.
- **FR-010**: The API MUST communicate retry semantics only via HTTP status codes and standard headers (not via custom body fields).
- **FR-011**: All endpoints under `/v1/*` MUST reference the shared ErrorResponse schema in the public API documentation for relevant non-2xx responses.
- **FR-012**: The API MUST include automated tests that validate:
  - Happy path for at least one endpoint (baseline: `/v1/ping`).
  - Validation failure flow returns the correct status and ErrorResponse schema.
  - Unhandled exception flow returns the correct status and ErrorResponse schema.
- **FR-013**: ErrorResponse `message` MUST be safe for clients (no stack traces, secrets, or sensitive internal identifiers).

### Key Entities *(include if feature involves data)*

- **ErrorResponse**: Standard error envelope returned for failure outcomes; includes `code` (stable identifier), `message` (safe human summary), and optional `details` (structured diagnostics).
- **Error Code**: A stable string identifier representing a class of error (e.g., validation failure, not found, rate limited) with a documented meaning and typical associated HTTP status.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All documented `/v1/*` endpoints return ErrorResponse-shaped bodies for their documented failure responses, verified by automated tests.
- **SC-002**: Client integrations can implement branching logic using only documented `code` values (no message parsing), validated by contract-style automated tests.
- **SC-003**: No publicly documented API error response includes a `retryable` field, verified by automated tests.
- **SC-004**: For the primary happy-path endpoint (`/v1/ping`), at least 95% of requests complete in under 1 second in a local smoke test.
- **SC-005**: For common failure flows (validation failure and internal error), at least 95% of requests complete in under 1 second in a local smoke test.

## Assumptions

- Existing endpoint `/v1/ping` remains the minimal “happy path” example used to validate the contract.
- The documented error code strings are UPPER_SNAKE_CASE and are treated as stable identifiers.
- When a precise retry time is not known, the API may omit retry timing headers rather than guessing.
- Clients may log or display `message` to end users; therefore `message` is always safe and non-sensitive.

## Dependencies

- Public API documentation must be updated whenever new error codes are added.
- Automated tests must run in CI and validate both success and failure flows against the documented contract.
