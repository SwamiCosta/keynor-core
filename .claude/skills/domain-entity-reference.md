# Skill: Domain Entity Reference

> Canonical field reference for keynor-core's universe entities and the `Era` class. Read before creating or modifying any entity, DTO, or domain service.

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
| `Lore` | `LoreCategory` | HISTORY, MYTH, LAW, PROPHECY, GEOGRAPHY, PHILOSOPHY |

`Place` additionally has `mapType: MapType` (NAVIGABLE or ABSTRACT).

### Request DTO field names

The domain model uses a `Timeline` value object with `founded` and `destroyed` fields. In the JSON body of API requests, these fields are **flattened** into the DTO with the following names:

| Domain field | JSON / DTO field name | Required | Notes |
|--------------|-----------------------|----------|-------|
| `timeline.founded` | `timelineFoundedEra` | Yes (`@NotBlank`) | Era string |
| `timeline.destroyed` | `timelineDestroyedEra` | No | Era string, nullable |

This mapping applies uniformly to all `Create*Request` and `Update*Request` DTOs:
`CreateCharacterRequest`, `UpdateCharacterRequest`, `CreatePlaceRequest`, `UpdatePlaceRequest`, `CreateFactionRequest`, `UpdateFactionRequest`, `CreateItemRequest`, `UpdateItemRequest`, `CreateEventRequest`, `UpdateEventRequest`, `CreateLoreRequest`, `UpdateLoreRequest`.

> Do **not** use `timeline.founded` or `timeline.destroyed` in the JSON body — these will produce a `400 Bad Request`.

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

## Era entity

`Era` is **not** a `UniverseEntity` subclass — it does not have `status`, `timeline`, `tags`, `images`, `categories`, or `body`. It is a standalone domain class that models both era intervals and single-moment temporal points on the same timeline.

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key, set at construction, immutable |
| `name` | String | Display name, unique |
| `orderIndex` | int | Determines position in the ordered timeline list |
| `type` | `EraType` | `ERA` (interval) or `POINT` (single moment) |
| `importance` | `EraImportance` | `STANDARD` or `MAJOR` — required when `type = POINT`, must be null when `type = ERA` |
| `description` | String | Optional descriptive text |
| `createdAt` | Instant | Set at construction, immutable |
| `updatedAt` | Instant | Updated on every mutation |

### Domain invariant

The `Era` constructor enforces: `importance` is required when `type = POINT`; must be null when `type = ERA`. Violation throws `IllegalArgumentException`.

### API response shape

`GET /api/public/v1/eras` returns all entries (eras and points) ordered by `orderIndex`:

```json
[
  { "id": "...", "name": "Age of Creation", "order": 1, "type": "ERA", "importance": null, "description": "..." },
  { "id": "...", "name": "The Great Sundering", "order": 2, "type": "POINT", "importance": "MAJOR", "description": "..." }
]
```

### Port naming

`EraRepository` output port exposes `findAllOrderedByIndex()` (not the generic `findAll`) to make the ordering contract explicit at the domain boundary.

---

## Archetype and Sign entities

Like `Era`, `Archetype` and `Sign` are **not** `UniverseEntity` subclasses — no `status`, `timeline`, `tags`, `images`, or `categories`. Both are closed reference/lookup sets (5 archetypes, 13 signs) modeling the Aelimic cosmology and are read-only at the API level — no create/update/delete use cases or internal controller exist for either.

### Archetype fields

| Field | Type | Description |
|-------|------|--------------|
| `id` | UUID | Primary key, set at construction, immutable |
| `name` | String | Display name, unique (e.g. "Communication") |
| `element` | String | Nullable — null only for `Obsession` |
| `suit` | String | Nullable — null only for `Obsession` |
| `vocation` | String | Nullable — null only for `Obsession` |
| `temperament` | String | Nullable — null only for `Obsession` |
| `cognitiveFunction` | String | Nullable — null only for `Obsession` |
| `selfRelation` | String | Nullable — null only for `Obsession` |
| `description` | String | Descriptive text |
| `createdAt` / `updatedAt` | Instant | Set at construction / updated on mutation |

### Sign fields

| Field | Type | Description |
|-------|------|--------------|
| `id` | UUID | Primary key, set at construction, immutable |
| `name` | String | Display name, unique (e.g. "The Rings") |
| `signOrder` | int | Position 1–13 in the seasonal cycle (13 = the Rift) |
| `seasonTime` | String | When in the year the sign falls |
| `archetypeId` | UUID | `NOT NULL` FK to `archetypes.id` |
| `subArchetype` | String | Nullable — null only for The Rift (sign 13), which has no sub-archetype |
| `summary` | String | Short one-line description |
| `body` | String | Full descriptive text |
| `createdAt` / `updatedAt` | Instant | Set at construction / updated on mutation |

`SignResponse.archetypeId` is a plain `UUID`, not a nested `Archetype` object — callers that need archetype details make a second call to `GET /api/public/v1/archetypes/{id}`.

### Port naming

`ArchetypeRepository` exposes `findAll()`. `SignRepository` exposes `findAllOrderedBySignOrder()` (not the generic `findAll`), mirroring `EraRepository`'s explicit-ordering convention.

---

*Maintained by Imaws. Update whenever a domain field, category enum, or status invariant changes.*
