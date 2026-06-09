# keynor-core — Logging Conventions

> Project-level implementation detail for the workspace-wide Skill 08 (Logging Conventions).
> Read this file whenever adding or modifying any logging-related code in keynor-core.

---

## Framework

SLF4J with Logback. Both are on the classpath via `spring-boot-starter-web` — no additional dependencies required.

Logger declaration (no Lombok; Lombok is not in the `pom.xml`):

```java
private static final Logger log = LoggerFactory.getLogger(YourClass.class);
```

---

## Logging points

### 1. Request boundary — `RequestLoggingFilter`

**Location:** `infrastructure/web/filter/RequestLoggingFilter.java`

**What it does:**
- Generates a UUID `traceId` per request and puts it in `MDC` via `MDC.put("traceId", traceId)`
- Logs `INFO` on entry: `"→ {} {} from {}"` (method, URI, remote address)
- Logs `INFO` on exit: `"← {} {} {}ms"` (method, URI, elapsed millis)
- Always clears MDC in a `finally` block: `MDC.clear()`

**Every log line** produced during a request carries the `traceId` automatically via the Logback pattern (see `logback-spring.xml`).

### 2. Exception handling — `GlobalExceptionHandler`

**Location:** `infrastructure/web/handler/GlobalExceptionHandler.java`

| Exception | HTTP Status | Log Level |
|-----------|-------------|-----------|
| `EntityNotFoundException` | 404 | WARN |
| `DuplicateEntityNameException` | 409 | WARN |
| `InvalidStatusTransitionException` | 422 | WARN |
| `IllegalArgumentException` | 400 | WARN |
| `MethodArgumentNotValidException` | 400 | WARN |
| `Exception` (catch-all) | 500 | ERROR with full stack trace |

**Rule:** The catch-all `Exception` handler must never expose internal details (stack trace messages, IP addresses, DB connection strings) in the `ProblemDetail` response body. It always returns the generic message `"An unexpected error occurred"`.

### 3. Status transitions — `UniverseEntity.changeStatus()`

**Location:** `domain/model/shared/UniverseEntity.java`

Logs at INFO level after every successful status transition:

```
"Status changed: entity={} from={} to={}"
```

**Placement decision:** Logging lives in `UniverseEntity` (domain layer) rather than in controllers or domain services. Rationale: the status transition is enforced in `changeStatus()` — this is the single authoritative point where a transition is accepted. Logging there guarantees every successful transition is recorded exactly once, regardless of which use case or controller triggered it. SLF4J carries no framework annotations and is acceptable in the domain layer per workspace Skill 08.

---

## Configuration — `logback-spring.xml`

**Location:** `src/main/resources/logback-spring.xml`

### Dev profile (`--spring.profiles.active=dev`)

- Root level: `DEBUG`
- Pattern (human-readable): `%d{HH:mm:ss.SSS} [%thread] [traceId=%X{traceId}] %-5level %logger{36} - %msg%n`

### Default profile (non-dev / production)

- Root level: `INFO`
- `org.springframework` suppressed to `WARN` (reduces noise)
- Pattern (structured key=value): `time=%d{ISO8601} thread=%thread traceId=%X{traceId} level=%-5level logger=%logger{36} msg="%msg"%n`

---

## What must never be logged

- Passwords, tokens, secrets — under any circumstance
- Full request or response bodies
- Stack traces at INFO or WARN level (only at ERROR)

---

## Tests

- `RequestLoggingFilterTest` verifies MDC population, uniqueness per request, and cleanup after chain completion (including on exception)
- `GlobalExceptionHandlerTest` verifies HTTP status codes and log levels for every registered handler

---

*Last updated: 2026-06-09*
