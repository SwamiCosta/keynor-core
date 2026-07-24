# keynor-core — Agent Context

> Project-level context for AI agents operating in keynor-core.
> Read this file and `../ARCHITECTURE.md` before executing any task.

---

## What this project is

`keynor-core` is the central API of the Keynor ecosystem. It is the authoritative source of truth for all universe entities — characters, places, factions, items, events, and lore. Every other service that needs universe data communicates through keynor-core.

---

## Responsibilities

- CRUD for all universe entities (characters, places, factions, items, events, lore)
- Serve data to all other services (aniannoth-overview, keynor-rpg, keynor-stories, summon-server)
- Central authentication and authorization for the ecosystem
- Enforce entity status rules (`canon`, `draft`, `deprecated`)

---

## Stack

| Concern | Technology |
|---------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Database | PostgreSQL (Flyway migrations) |
| Build tool | Maven |
| Auth | Spring Authorization Server (OAuth2) + Resource Server (JWT) |
| API style | REST — `ProblemDetail` (RFC 7807) for error responses |
| Testing | JUnit 5 + Mockito + Testcontainers |
| Logging | SLF4J + Logback (via Spring Boot) — see `.claude/skills/logging-conventions.md` |

---

## Local environment assumptions

The user always has Docker Compose running (PostgreSQL) and an application instance already up before invoking any agent in this project.

- **Use what is already running.** Never start, stop, or restart the database, the application, or any container — and never provision a disposable substitute (e.g. `docker run maven:...` as a stand-in for a missing local JDK).
- **If something required is not running or not reachable**, stop and report it to the user. Do not work around it by starting something new.
- Applies to Imperium (compile/test against the running app), Judis (Testcontainers, `mvn verify`), and Siegmund (`pg_dump`, diagnostic `SELECT`s against the running database).

---

## Architecture

keynor-core follows **hexagonal architecture** (ports & adapters). The domain layer has zero framework dependencies.

```
keynor-core/
├── src/
│   ├── main/
│   │   └── java/com/keynor/core/
│   │       ├── domain/                  ← pure domain (entities, value objects, exceptions)
│   │       │   ├── model/
│   │       │   ├── port/
│   │       │   │   ├── in/              ← input ports (use case interfaces)
│   │       │   │   └── out/             ← output ports (repository interfaces)
│   │       │   └── service/             ← domain services (implement input ports)
│   │       ├── application/             ← application layer (orchestration, DTOs)
│   │       │   ├── dto/
│   │       │   └── usecase/
│   │       └── infrastructure/          ← adapters (Spring, JPA, Security, etc.)
│   │           ├── web/                 ← REST controllers (input adapters)
│   │           ├── persistence/         ← JPA repositories (output adapters)
│   │           └── security/            ← Spring Security configuration
│   └── test/
│       └── java/com/keynor/core/
│           ├── domain/                  ← unit tests for domain services
│           └── infrastructure/          ← integration tests for adapters
└── pom.xml
```

### Layer rules

| Layer | Depends on | Never depends on |
|-------|-----------|-----------------|
| `domain` | nothing | Spring, JPA, any framework |
| `application` | `domain` | infrastructure adapters |
| `infrastructure` | `application`, `domain` | — |

---

## Domain entities

Universe entities (`Character`, `Place`, `Faction`, `Item`, `Event`, `Lore`) extend `UniverseEntity` and share a common field set, status transition rules, and deletion policy. The `Era` class models temporal eras and points on the same timeline and is **not** a `UniverseEntity` subclass.

For the full field reference, category enums, request DTO field mapping, status transition rules, and Era's fields/invariant/API shape, see `.claude/skills/domain-entity-reference.md`.

---

## Security model

keynor-core is both **Authorization Server** and **Resource Server**, with two roles (`ADMIN`, `SYSTEM`) and 3 ordered `@Order` filter chains. No default users or clients exist in the schema — they must be inserted manually before the API can be used.

For roles, token flow, the filter chain ordering table (including the critical Resource-Server-before-Form-Login constraint), CORS, the full bootstrap procedure, and token acquisition examples, see `.claude/skills/security-model.md`.

---

## Public API

Endpoints under `/api/public/v1/` require no authentication and are consumed by aniannoth-overview.

### Invariants

- All public endpoints return **only `CANON` entities** — DRAFT and DEPRECATED are never exposed
- Responses always follow the `PagedResponse<T>` shape: `content`, `page`, `size`, `totalElements`
- `findById` endpoints are included on all public controllers

**Exception — closed reference/lookup sets:** `Era`, `Archetype`, and `Sign` are not `UniverseEntity` subclasses. They have no `status` field (so the CANON-only filter does not apply) and are fixed, non-paginated sets, so their list endpoints return a plain JSON array of all entries instead of `PagedResponse<T>`, and accept no query parameters. See `.claude/skills/domain-entity-reference.md`.

**Exception — batch id lookup:** `GET /api/public/v1/characters/batch?ids=uuid1,uuid2,...` (added to resolve `Faction.members` ids into displayable name/status on the frontend) deliberately does not filter by CANON either — it returns every requested id that exists, whatever its status, so the frontend can gray out non-canon members instead of silently losing them. This mirrors the pre-existing, unfiltered behavior of `entity_links` resolution (`UniverseEntityLookupJpaAdapter.findSummary`, used to populate the `links` field on every public response), not a new departure. Response is a bare array of `LinkedEntityResponse` (`type`, `id`, `name`, `status`) — no pagination, no `language` param (each id already pins a specific language row).

### Available endpoints

| Controller | Endpoints |
|------------|-----------|
| `PublicCharacterController` | `GET /api/public/v1/characters`, `GET /api/public/v1/characters/{id}`, `GET /api/public/v1/characters/batch?ids=...` |
| `PublicPlaceController` | `GET /api/public/v1/places`, `GET /api/public/v1/places/{id}` |
| `PublicFactionController` | `GET /api/public/v1/factions`, `GET /api/public/v1/factions/{id}` |
| `PublicItemController` | `GET /api/public/v1/items`, `GET /api/public/v1/items/{id}` |
| `PublicEventController` | `GET /api/public/v1/events`, `GET /api/public/v1/events/{id}` |
| `PublicLoreController` | `GET /api/public/v1/lore`, `GET /api/public/v1/lore/{id}` |
| `PublicEraController` | `GET /api/public/v1/eras`, `GET /api/public/v1/eras/{id}` |
| `PublicMapController` | `GET /api/public/v1/maps`, `GET /api/public/v1/maps/{id}` |
| `PublicArchetypeController` | `GET /api/public/v1/archetypes`, `GET /api/public/v1/archetypes/{id}` |
| `PublicSignController` | `GET /api/public/v1/signs`, `GET /api/public/v1/signs/{id}` |

### Query parameters (list endpoints)

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `categories` | `List<String>` | — | Filter by one or more category values |
| `page` | `int` | `0` | Zero-based page number |
| `size` | `int` | `20` | Page size |

### Adding a new public controller

For the step-by-step procedure, see `.claude/skills/public-controller-checklist.md`.

---

## Database migrations (Flyway)

For the migration changelog (V1–V8), see `.claude/skills/migration-history.md`. For the full procedure (authorization gate, destructive-operations rules), see the workspace `SKILLS.md` — Skill 02.

---

## Cross-entity links (`entity_links`)

Any universe entity can reference any other universe entity — e.g. a `Lore` entry that mentions two `Character`s renders as a list of clickable links in aniannoth-overview. This is modeled as a **polymorphic join table** (no real FKs to the six entity tables), already wired end-to-end for all 6 entity types.

For the schema, domain model, reference implementation, and the FE field naming contract, see `.claude/skills/entity-links-implementation.md`.

---

## Hidden content (`hidden`, `hidden_content_lock`)

A visibility dimension orthogonal to `status`, powering the Hidden Content & Black Pins cross-project feature (see root `ARCHITECTURE.md`). A hidden entity is always `status = CANON` but is excluded from every public list/browse endpoint regardless — the only routes to it are a black pin or a link from another already-unlocked hidden entity.

**Rule that content authors must never violate:** a hidden entity may link to a visible one; a visible entity may never link to hidden content. Enforced server-side and rejected at write time — see `aroneus.md` for the authoring-facing explanation.

Currently wired for `Character` and `Lore` only. For the full schema, domain wiring, and the replication steps for `Place`/`Faction`/`Item`/`Event`, see `.claude/skills/hidden-content-implementation.md`.

---

## Universe content seed

`db/seed/universe-content.sql` is the single source of truth for all universe content data. It uses TRUNCATE + INSERT and represents the complete current state of all universe tables.

- **Updated on demand** — Siegmund regenerates it when the user requests after a data change
- **Applied manually by the user** — agents never execute it against any database
- **Divergence diagnosis** — Siegmund can inspect the current DB state via SELECT queries and compare against the file to detect what is missing or outdated

For the full procedure, see `.claude/skills/universe-content-dump.md`.

---

## Domain wiring

Domain services have zero Spring annotations. They are wired as Spring beans in `DomainConfiguration` (infrastructure/config), which is the only class that knows both the domain services and the output port adapters.

Pattern:
```java
@Bean
public CharacterService characterService(CharacterRepository characterRepository) {
    return new CharacterService(characterRepository);
}
```

Controllers depend only on the use case interfaces (`CreateCharacterUseCase`, etc.), never on concrete service classes.

---

## Coding conventions

Follows workspace-wide Clean Code rules plus Java-specific:

- Classes: `PascalCase`
- Methods and variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `com.keynor.core.<layer>.<module>`
- No abbreviations — full descriptive names
- One class per file
- Use records for DTOs and value objects where immutability fits
- Prefer constructor injection over field injection

### Naming patterns

| Artifact | Pattern | Example |
|----------|---------|---------|
| Input port | `UseCase` | `CreateCharacterUseCase` |
| Output port | `Repository` | `CharacterRepository` |
| Domain service | `Service` | `CharacterService` |
| Internal REST controller | `Internal*Controller` | `InternalCharacterController` |
| Public REST controller | `Public*Controller` | `PublicCharacterController` |
| JPA entity | `Entity` | `CharacterEntity` |
| JPA adapter | `JpaAdapter` | `CharacterJpaAdapter` |
| DTO (request) | `Request` | `CreateCharacterRequest` |
| DTO (response) | `Response` | `CharacterResponse` |

### Controller structure

Controllers live under `infrastructure/web/<domain>/` — one directory per domain model:

```
infrastructure/web/
├── character/
│   ├── InternalCharacterController.java   ← authenticated, full CRUD
│   └── PublicCharacterController.java     ← unauthenticated, read-only CANON
├── place/
│   ├── InternalPlaceController.java
│   └── PublicPlaceController.java
├── era/
│   └── PublicEraController.java           ← no internal controller yet
├── map/
│   └── PublicMapController.java
├── archetype/
│   └── PublicArchetypeController.java     ← no internal controller — closed reference set
├── sign/
│   └── PublicSignController.java          ← no internal controller — closed reference set
└── handler/
    └── GlobalExceptionHandler.java
```

- `Internal*Controller` → mapped to `/api/v1/<domain>`, requires JWT
- `Public*Controller` → mapped to `/api/public/v1/<domain>`, `permitAll`
- Both controllers for the same domain share the same package (`web.<domain>`)

### Known simple-name conflicts (import rules)

See `.claude/skills/jpa-adapter-checklist.md` for the PageRequest naming conflict and the required import pattern for `*JpaAdapter` classes.

---

## Logging

All logging in keynor-core follows workspace Skill 08. The project-level implementation details are in `.claude/skills/logging-conventions.md`.

Key points:
- Logger declaration: `private static final Logger log = LoggerFactory.getLogger(YourClass.class);` (no Lombok)
- Every HTTP request gets a UUID `traceId` injected into MDC by `RequestLoggingFilter`
- Exception levels: WARN for expected client errors (4xx), ERROR with stack trace for unexpected failures (5xx)
- Status transitions are logged at INFO in `UniverseEntity.changeStatus()` — single authoritative point
- Two log profiles: `dev` (DEBUG, human-readable) and default/prod (INFO, structured key=value)

---

## Testing rules

- All new code must have tests
- **Unit tests**: domain services — no Spring context, no database
- **Integration tests**: adapters — use Testcontainers for real PostgreSQL
- Test class naming: `*Test.java`
- Integration test class naming: `*IntegrationTest.java`
- Tests must not share mutable state between test methods

---

## Agent structure

```
keynor-core/
└── .claude/
    ├── CLAUDE.md              ← this file
    └── agents/
        ├── imaws.md           ← Level 3 — project architect
        ├── imperium.md        ← Level 2 — Java code developer
        ├── siegmund.md        ← Level 2 — database data and seed scripts
        └── judis.md           ← Level 2 — unit and integration tests
```

| Agent | Level | Scope |
|-------|-------|-------|
| Imaws | 3 — Architect | Architecture, cross-cutting concerns, proposals for protected changes |
| Imperium | 2 — Developer | Java source code: domain, application, infrastructure layers |
| Siegmund | 2 — Developer | SQL seed and maintenance scripts; read-only DB queries |
| Judis | 2 — Developer | Unit tests (Mockito) and integration tests (Testcontainers) |

---

## Agent operational rules

Before beginning the task itself — reading project source or task-specific documentation, implementing features, creating branches, running commands, or opening PRs — every agent must:

1. Switch to `main`: `git checkout main`
2. Pull the latest changes: `git pull`

This does not apply to the agent's own fixed mandatory reading (`ARCHITECTURE.md`, the root `CLAUDE.md`, `SKILLS.md`, this file, the agent's own `.md` file, and any Always-tier skill file) — reading those is how an agent learns this very rule, not an action on the project's current state. Sync once the agent moves on to the task itself.

A second pull is not required within the same task session. See workspace `SKILLS.md` — Skill 09.

---

## FAQ for agents

**Can I add a Spring annotation to a domain entity?**
No. The domain layer has zero framework dependencies. Use a separate JPA entity in the infrastructure layer and map between them.

**Can I add a new Maven dependency?**
No. Adding dependencies is a protected action — propose it to the user and wait for authorization.

**Can I create a new database migration?**
Follow the workspace `SKILLS.md` — Skill 02.

**Can a domain service use `@Service`?**
No. Domain services are annotation-free. Register them as `@Bean` in `DomainConfiguration` in the infrastructure/config package.

**What is the base package?**
`com.keynor.core`

**How do I edit a CLAUDE.md, agent file, or SKILLS.md?**
Follow the workspace `SKILLS.md` — Skill 01.

---

*Last updated: 2026-07-24 (added the "Hidden content" section documenting `hidden`/`hidden_content_lock` and the one-way linking rule, alongside the new `.claude/skills/hidden-content-implementation.md` — see keynor-core PR #88. Previous entry, 2026-07-15: documented the `GET /api/public/v1/characters/batch` endpoint as the second exception to the CANON-only public API invariant, alongside the pre-existing Era/Archetype/Sign closed-set exception — see keynor-core PR #71)*
