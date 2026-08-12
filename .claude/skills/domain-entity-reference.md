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
| `summary` | String | Short description |
| `body` | String | Full content in Markdown |
| `status` | EntityStatus | `CANON`, `DRAFT`, or `DEPRECATED` |
| `timeline` | Timeline | Value object with `founded` and `destroyed` (era strings, nullable) |
| `createdAt` | Instant | Set at construction, immutable |
| `updatedAt` | Instant | Updated on every mutation |
| `language` | Language | `EN` or `PT` — which language this row's text is written in |
| `translationGroupId` | UUID | Links this row to its counterpart(s) in other languages; not a one-directional "original" FK — a shared group id |
| `hidden` | boolean | Visibility dimension orthogonal to `status` (V17). A hidden entity is always `CANON` but is excluded from every public list/browse query regardless — the sole route to it in the UI is a black pin or a link from another already-unlocked hidden entity. See root `ARCHITECTURE.md` — "Cross-Project Feature: Hidden Content & Black Pins" |
| `common` | boolean | Visibility dimension independent of `status` and orthogonal to `hidden` (V19). A common entity is excluded from every public list/browse query and from every map pin, but — unlike `hidden` — carries no status invariant and no lock (no riddle/password); `findById` and links-resolution stay open and unredacted. The sole route to it in the UI is a link from another already-visible entity. See `.claude/skills/common-content-implementation.md` |
| `versionGroupId` | UUID | Links this row to its "version" counterpart(s) — separate entity rows representing successive narrative versions of the same element (e.g. a Character continued in a later era, same display `name`, own `body`/`summary`/timeline). Same shape as `translationGroupId`: a shared group id, not a directional FK (V18). Deliberately independent of `hidden`, `status`, and `language` — a version group may freely mix hidden and non-hidden rows, any status, any language; no constraint enforces homogeneity. Distinct from the optional, visual `entity_links` relation an author may additionally create between two versions — that is never required for `versionGroupId` to do its job |

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

`Faction` additionally has `members: List<UUID>` — the ids of `Character` entities belonging to the faction. Optional on create/update (defaults to empty), order-preserving, exposed on both `InternalFactionController` and `PublicFactionController` responses. Persisted via a dedicated `faction_members` join table (V15) rather than the polymorphic `entity_links` table (V7) — see `migration-history.md` for why: membership is order-preserving and strongly typed to a single entity pair, unlike `entity_links`'s unordered any-to-any references.

### Request DTO field names

The domain model uses a `Timeline` value object with `founded` and `destroyed` fields. In the JSON body of API requests, these fields are **flattened** into the DTO with the following names:

| Domain field | JSON / DTO field name | Required | Notes |
|--------------|-----------------------|----------|-------|
| `timeline.founded` | `timelineFoundedEra` | Yes (`@NotBlank`) | Era string |
| `timeline.destroyed` | `timelineDestroyedEra` | No | Era string, nullable |

This mapping applies uniformly to all `Create*Request` and `Update*Request` DTOs:
`CreateCharacterRequest`, `UpdateCharacterRequest`, `CreatePlaceRequest`, `UpdatePlaceRequest`, `CreateFactionRequest`, `UpdateFactionRequest`, `CreateItemRequest`, `UpdateItemRequest`, `CreateEventRequest`, `UpdateEventRequest`, `CreateLoreRequest`, `UpdateLoreRequest`.

> Do **not** use `timeline.founded` or `timeline.destroyed` in the JSON body — these will produce a `400 Bad Request`.

### Multilingual fields (`language` / `translationGroupId`)

Every `Create*Request` DTO carries `@NotBlank String language` (parsed via `LanguageRequestParser.parse()`, case-insensitive, only `EN`/`PT` allowed — any other value throws `IllegalArgumentException`) and an optional `UUID translationGroupId`, positioned after `status` and before `links`.

| `translationGroupId` in request body | Result |
|---------------------------------------|--------|
| absent / `null` | the new entity becomes its own group anchor — `translationGroupId` defaults to the entity's own newly generated `id` |
| a UUID of an existing row's group | the new entity joins that translation group (e.g. submitting the PT counterpart of an existing EN entity) |

This is the mechanism Aroneus uses to submit a translation pair, and the same one Siegmund/Lethra rely on to detect an entity with no translation yet (its group has fewer distinct `language` values than the 2 supported).

**`language` shows up differently across the three request shapes — do not conflate them:**

| Request shape | `language` present? | How |
|----------------|---------------------|-----|
| `Create*Request` (`POST`) | Yes, required | `@NotBlank` field in the request body |
| list / `findAll` (`GET`) | Yes, required | `@RequestParam String language`, no default — `400` if omitted. Applies to every list endpoint, public and internal, for all 6 `UniverseEntity` types plus `Era`, `Archetype`, `Sign` |
| `findById` (`GET /{id}`) | No | The id already pins a single row's language; no filter needed |
| `Update*Request` (`PUT`) | No | A row's `language` is immutable after creation — there is no field for it on any `Update*Request` DTO. To add the other language, submit a new `Create*Request` with `translationGroupId` set to the existing row's id, not a `PUT` on the existing row |

See `keynor-core/CLAUDE.md` — "Query parameters (list endpoints)" for the full parameter table.
### Version fields (`versionGroupId`)

Every `Create*Request` DTO also carries an optional `UUID versionGroupId`, positioned alongside `translationGroupId`. Same defaulting semantics:

| `versionGroupId` in request body | Result |
|-----------------------------------|--------|
| absent / `null` | the new entity becomes its own group anchor — `versionGroupId` defaults to the entity's own newly generated `id` |
| a UUID of an existing row's group | the new entity joins that version group (e.g. submitting "Character v2" as the continuation of an already-existing "Character v1") |

`versionGroupId` is create-only — there is no update path for it, the same restriction `translationGroupId` has. It is orthogonal to `hidden`, `status`, and `language`: a version group may mix hidden and non-hidden rows, any status, any language, with no constraint enforcing consistency across the group. It is also independent of the optional, visual `entity_links` relation — an author may additionally link two versions so they show as Related Entities in aniannoth-overview, but that link is never required for the two rows to belong to the same version group.

**Querying a full version history** (e.g. "every version of John Silver, across eras") is not exposed as a dedicated API endpoint — group the relevant table by `version_group_id` (e.g. `SELECT * FROM characters WHERE version_group_id = :id`) via an ad hoc query, the same way Siegmund already answers similar one-off data questions. See `aroneus.md` for how content submissions should recognize and set this field.

### Optional `status` field on creation

Every `Create*Request` DTO (`CreateCharacterRequest`, `CreatePlaceRequest`, `CreateFactionRequest`, `CreateItemRequest`, `CreateEventRequest`, `CreateLoreRequest`) carries an optional `status: String` field, positioned between `timelineDestroyedEra` and `links`. It has no Jakarta Validation annotation — there is nothing to reject at the DTO level, since absence is itself a valid value.

Semantic parsing and defaulting happen in the web layer, via the shared `EntityStatusRequestParser.parseCreationStatus(String)` helper (`infrastructure/web/shared/`), called identically by all six `Internal*Controller.create()` methods:

| `status` in request body | Result |
|---------------------------|--------|
| absent / `null` | defaults to `DRAFT` |
| `"draft"` / `"DRAFT"` (case-insensitive) | `DRAFT` |
| `"canon"` / `"CANON"` (case-insensitive) | `CANON` |
| `"deprecated"` / `"DEPRECATED"` (case-insensitive) | rejected — throws `IllegalArgumentException` ("Status DEPRECATED is not allowed on creation. Allowed values: DRAFT, CANON") |
| any other value | rejected — throws `IllegalArgumentException` from `EntityStatus.valueOf()` |

`Update*Request` DTOs do not carry a `status` field — status changes after creation go through `UniverseEntity.changeStatus()` and the transition rules below, not the create flow.

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

`Era` is **not** a `UniverseEntity` subclass — it does not have `status`, `timeline`, `images`, `categories`, or `body`. It is a standalone domain class that models both era intervals and single-moment temporal points on the same timeline.

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
| `language` | Language | `EN` or `PT` — same multilingual mechanism as `UniverseEntity` (see above) |
| `translationGroupId` | UUID | Same shared-group semantics as `UniverseEntity` |

`eras.name` keeps its pre-existing `UNIQUE` constraint (not widened to `UNIQUE(name, language)`) — a PT era's name is itself translated text, so it never literally collides with its EN counterpart's name.

**`links` (2026-07-30):** every `Era`/`POINT` entry now resolves an `entity_links`-backed `links` field (`List<LinkedEntityResponse>`), exactly like a `UniverseEntity`'s `links` field, letting an era or point's description reference real entities (e.g. "the era in which the Amets first appeared" linking to that Faction). `Era` itself is still not a `UniverseEntity` subclass and gained no new domain field — `links` is resolved on read via `FindLinkedEntitiesUseCase.findLinks(EntityType.ERA, id)`, same as every other entity. **One-directional only:** `EntityType.ERA` is valid as an `entity_links` `source_type`, never as a `target_type` — `UniverseEntityLookupJpaAdapter.findSummary(ERA, id)` throws `UnsupportedOperationException` if ever called, since nothing in the codebase creates a link with `target_type = ERA`. `CreateEraRequest`/`CreateEraUseCase.Command` gained a `links: List<EntityLinkRequest>`/`List<EntityLinkRef>` field, submitted and replaced the same way `Lore`'s links are (see `entity-links-implementation.md`). **`UpdateEraUseCase` (2026-08-01):** `PUT /api/v1/eras/{id}` now exists — `name`, `orderIndex`, `type`, `importance`, `description`, and `links` are all updatable (full-replacement semantics, same as every other `Update*Request`); `id`, `createdAt`, `language`, and `translationGroupId` remain immutable. There is still no status or delete endpoint for `Era`.

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

Like `Era`, `Archetype` and `Sign` are **not** `UniverseEntity` subclasses — no `status`, `timeline`, `images`, or `categories`. Both are closed reference/lookup sets (5 archetypes, 13 signs) modeling the Aelimic cosmology and are read-only at the API level — no create/update/delete use cases or internal controller exist for either.

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
| `language` | Language | `EN` or `PT` — same multilingual mechanism as `UniverseEntity` (see above) |
| `translationGroupId` | UUID | Same shared-group semantics as `UniverseEntity` |

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
| `language` | Language | `EN` or `PT` — same multilingual mechanism as `UniverseEntity` (see above) |
| `translationGroupId` | UUID | Same shared-group semantics as `UniverseEntity` |

`SignResponse.archetypeId` is a plain `UUID`, not a nested `Archetype` object — callers that need archetype details make a second call to `GET /api/public/v1/archetypes/{id}`.

### Port naming

`ArchetypeRepository` exposes `findAll()`. `SignRepository` exposes `findAllOrderedBySignOrder()` (not the generic `findAll`), mirroring `EraRepository`'s explicit-ordering convention.

---

*Maintained by Imaws. Update whenever a domain field, category enum, or status invariant changes.*
