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

All universe entities extend `UniverseEntity` (abstract base class):

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key, set at construction, immutable |
| `name` | String | Display name |
| `categories` | List\<CategoryEnum\> | One or more categories — an entity can have multiple (e.g. DEITY + HERO) |
| `tags` | List\<String\> | Searchable free-form tags |
| `summary` | String | Short description |
| `body` | String | Full content in Markdown |
| `status` | EntityStatus | `CANON`, `DRAFT`, or `DEPRECATED` |
| `timeline` | Timeline | Value object with `founded` and `destroyed` (era strings, nullable) |
| `createdAt` | Instant | Set at construction, immutable |
| `updatedAt` | Instant | Updated on every mutation |

Entity types and their category enums:

| Entity | Category enum | Values |
|--------|--------------|--------|
| `Character` | `CharacterCategory` | HERO, VILLAIN, DEITY, CREATURE, NPC |
| `Place` | `PlaceCategory` | CITY, REGION, DUNGEON, REALM, STRUCTURE |
| `Faction` | `FactionCategory` | EMPIRE, GUILD, ORDER, TRIBE, DIVINE |
| `Item` | `ItemCategory` | WEAPON, ARTIFACT, RELIC, TOOL, CONSUMABLE |
| `Event` | `EventCategory` | BATTLE, POLITICAL, DIVINE, NATURAL, SOCIAL |
| `Lore` | `LoreCategory` | HISTORY, MYTH, LAW, PROPHECY, GEOGRAPHY |

`Place` additionally has `mapType: MapType` (NAVIGABLE or ABSTRACT).

### Status transition rules

Valid transitions enforced in `UniverseEntity.changeStatus()`:
- DRAFT → CANON ✓
- DRAFT → DEPRECATED ✓
- CANON → DRAFT ✓
- CANON → DEPRECATED ✓
- DEPRECATED → DRAFT ✓
- DEPRECATED → CANON ✗ (forbidden — throws `InvalidStatusTransitionException`)

### Deletion policy

Universe entities (lore/story data) support **hard delete**. User data (`users` table) must never be permanently deleted.

---

## Security model

keynor-core is both **Authorization Server** and **Resource Server**.

### Roles

| Role | Grantee | Grant type |
|------|---------|------------|
| `ADMIN` | Human users (admin panel / RPG integration) | `authorization_code` + PKCE, form login |
| `SYSTEM` | Service-to-service calls (keynor-rpg, aniannoth, etc.) | `client_credentials` |

Both roles have full access to all endpoints in the current phase. No hierarchy between them.

### Token flow

- Authorization Server exposes `/oauth2/token`, `/oauth2/authorize`, OIDC discovery
- All `/api/**` endpoints are protected and require a valid Bearer JWT — **except** `/api/public/**`, which is `permitAll`
- JWT is validated by the Resource Server filter chain
- RSA key pair (2048-bit) is generated at startup — **ephemeral for dev**. Must be externalized for production.
- OAuth2 clients and authorizations are persisted via `JdbcRegisteredClientRepository` / `JdbcOAuth2AuthorizationService`

### CORS

Allowed origins (configured in `ResourceServerConfig`):
- `http://localhost:5173` (aniannoth-overview dev server)
- `http://localhost:4173` (aniannoth-overview preview)

### First bootstrap

No default users or clients exist in the schema. Before using the API you must:
1. Insert a BCrypt-hashed ADMIN user in the `users` table
2. Insert a SYSTEM client in the `oauth2_registered_client` table

---

## Public API

Endpoints under `/api/public/v1/` require no authentication and are consumed by aniannoth-overview.

### Invariants

- All public endpoints return **only `CANON` entities** — DRAFT and DEPRECATED are never exposed
- Responses always follow the `PagedResponse<T>` shape: `content`, `page`, `size`, `totalElements`
- `findById` endpoints are included on all public controllers

### Available endpoints

| Controller | Endpoints |
|------------|-----------|
| `PublicCharacterController` | `GET /api/public/v1/characters`, `GET /api/public/v1/characters/{id}` |
| `PublicPlaceController` | `GET /api/public/v1/places`, `GET /api/public/v1/places/{id}` |
| `PublicFactionController` | `GET /api/public/v1/factions`, `GET /api/public/v1/factions/{id}` |
| `PublicItemController` | `GET /api/public/v1/items`, `GET /api/public/v1/items/{id}` |
| `PublicEventController` | `GET /api/public/v1/events`, `GET /api/public/v1/events/{id}` |
| `PublicLoreController` | `GET /api/public/v1/lore`, `GET /api/public/v1/lore/{id}` |
| `PublicEraController` | `GET /api/public/v1/eras`, `GET /api/public/v1/eras/{id}` |
| `PublicMapController` | `GET /api/public/v1/maps`, `GET /api/public/v1/maps/{id}` |

### Query parameters (list endpoints)

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `categories` | `List<String>` | — | Filter by one or more category values |
| `tags` | `List<String>` | — | Filter by one or more tags |
| `page` | `int` | `0` | Zero-based page number |
| `size` | `int` | `20` | Page size |

### Adding a new public controller

1. Create `Public*Controller` in `infrastructure/web/<domain>/` (same package as `Internal*Controller`)
2. Inject `FindAll*UseCase` and `FindById*UseCase` only
3. Fix the `EntityFilter` to `List.of(EntityStatus.CANON)` — never expose other statuses
4. Map with `PagedResponse.from(result, *Response::from)`
5. No new DTOs needed if the existing `*Response` already covers the required fields
6. Ask Judis to add unit tests (see `.claude/skills/unit-testing-controllers.md`)

---

## Database migrations (Flyway)

| Version | Description |
|---------|-------------|
| V1 | `users` table |
| V2 | 6 entity tables + 12 join tables (categories, tags) |
| V3 | Spring Authorization Server OAuth2 tables |
| V4 | `eras` and `maps` tables |

For the full procedure, see the workspace `SKILLS.md` — Skill 02.

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
└── handler/
    └── GlobalExceptionHandler.java
```

- `Internal*Controller` → mapped to `/api/v1/<domain>`, requires JWT
- `Public*Controller` → mapped to `/api/public/v1/<domain>`, `permitAll`
- Both controllers for the same domain share the same package (`web.<domain>`)

### Known simple-name conflicts (import rules)

The domain model and Spring share simple names that collide at compile time. **Violation causes a build error.** Follow these rules exactly — no exceptions:

| Domain type | Conflicting Spring type | Rule |
|-------------|------------------------|------|
| `com.keynor.core.domain.model.shared.PageRequest` | `org.springframework.data.domain.PageRequest` | Import only Spring's `PageRequest`. Use the fully-qualified domain name in method signatures and usages. |

**Correct pattern in any `*JpaAdapter.java`:**

```java
// Import Spring's PageRequest — NOT the domain one
import org.springframework.data.domain.PageRequest;

// Use FQN for the domain PageRequest in the method signature
public PageResult<Foo> findAll(FooFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest) {
    PageRequest springPage = PageRequest.of(pageRequest.page(), pageRequest.size());
    // ...
}
```

> **Before opening any PR** involving a `*JpaAdapter`: confirm the import block contains at most one of each conflicting pair.

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

Before analyzing or reporting on the current state of this project, every agent must:

1. Switch to `main`: `git checkout main`
2. Pull the latest changes: `git pull`

Analysis performed on stale or feature branches may produce incorrect assessments, duplicate work already merged, or miss critical recent changes.

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

**How do I handle the PageRequest naming conflict?**
`com.keynor.core.domain.model.shared.PageRequest` and `org.springframework.data.domain.PageRequest` share the same simple name. In JPA adapters, import Spring's `PageRequest` (the more frequent call) and use the fully-qualified domain name in method signatures:
```java
public PageResult<Character> findAll(EntityFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest)
```

**What is the base package?**
`com.keynor.core`

**How do I edit a CLAUDE.md, agent file, or SKILLS.md?**
Follow the workspace `SKILLS.md` — Skill 01.

---

*Last updated: 2026-06-08 — added Public API section, CORS origins, V4 migration entry, unit-testing-controllers skill*
