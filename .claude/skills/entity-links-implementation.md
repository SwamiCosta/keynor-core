# Skill: Entity Links Implementation

> Covers the `entity_links` polymorphic join table — schema, domain model, and the wiring pattern for cross-entity references. Read before touching anything related to linking one universe entity to another, or before replicating the link pattern to a new entity type.

---

## Cross-entity links (`entity_links`)

Any universe entity can reference any other universe entity — e.g. a `Lore` entry that mentions two `Character`s renders as a list of clickable links in aniannoth-overview. This is modeled as a **polymorphic join table**, independent of the six entity tables (no shared parent table exists, so a single FK-based relation per pair is not possible).

### Schema (V7)

```sql
CREATE TABLE entity_links (
    id          UUID        PRIMARY KEY,
    source_type VARCHAR(20) NOT NULL,
    source_id   UUID        NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id   UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_entity_link UNIQUE (source_type, source_id, target_type, target_id),
    CONSTRAINT chk_no_self_link CHECK (NOT (source_type = target_type AND source_id = target_id))
);
```

`source_type` / `target_type` are one of the `EntityType` enum values: `CHARACTER`, `PLACE`, `FACTION`, `ITEM`, `EVENT`, `LORE`. There are no real foreign keys to the six entity tables — referential integrity (existence, hard-delete cascade) is enforced in the domain/application layer, not the database.

### Domain model

| Class | Layer | Purpose |
|-------|-------|---------|
| `EntityType` | `domain/model/shared` | Enum identifying which entity table a reference points to |
| `EntityLinkRef` | `domain/model/shared` | Input value object `(targetType, targetId)` — used in Create/Update commands |
| `EntityLink` | `domain/model/shared` | Persisted link record `(id, sourceType, sourceId, targetType, targetId, createdAt)` |
| `EntityLinkSummary` | `domain/model/shared` | Resolved projection `(type, id, name, status)` used to render a link without a second round trip |
| `EntityLinkRepository` | `domain/port/out` | `findBySource`, `replaceLinks`, `deleteAllForEntity` |
| `UniverseEntityLookupRepository` | `domain/port/out` | `findSummary(EntityType, UUID)` — resolves name/status for any of the 6 entity types |
| `FindLinkedEntitiesUseCase` | `domain/port/in/shared` | Input port to resolve a source entity's outgoing links |
| `EntityLinkService` | `domain/service` | Implements `FindLinkedEntitiesUseCase`; combines `EntityLinkRepository` + `UniverseEntityLookupRepository` |

Infrastructure: `EntityLinkEntity` / `EntityLinkJpaRepository` / `EntityLinkMapper` / `EntityLinkJpaAdapter` (table `entity_links`) and `UniverseEntityLookupJpaAdapter` (switches on `EntityType` to query the matching `*JpaRepository`) live under `infrastructure/persistence/shared/`.

### Reference implementation: Lore

`Lore` is the first entity wired end-to-end and is the pattern to replicate for the other five:

- `CreateLoreUseCase.Command` / `UpdateLoreUseCase.Command` gained a `List<EntityLinkRef> links` field
- `LoreService` takes `EntityLinkRepository` as a second constructor dependency; calls `replaceLinks(EntityType.LORE, id, links)` after every create/update, and `deleteAllForEntity(EntityType.LORE, id)` after delete (cleans up links where the deleted Lore was source **or** target)
- `CreateLoreRequest` / `UpdateLoreRequest` gained `List<EntityLinkRequest> links` — `EntityLinkRequest` is `(targetType: String, targetId: UUID)`
- `LoreResponse.from(Lore, List<EntityLinkSummary>)` now takes the resolved links and maps them to `List<LinkedEntityResponse>` (`type`, `id`, `name`, `status`)
- `InternalLoreController` and `PublicLoreController` inject `FindLinkedEntitiesUseCase` and call `findLinks(EntityType.LORE, id)` to populate the response on every read

### Replicating to Character, Place, Faction, Item, Event

Apply the same five changes (Command, Service, Request DTOs, Response DTO, Controllers) per entity. `EntityLinkRequest`, `LinkedEntityResponse`, `EntityType`, `EntityLinkRef`, `EntityLinkRepository`, and `FindLinkedEntitiesUseCase` are already shared — no new shared infrastructure is needed, only wiring per entity. Register each `*Service` bean in `DomainConfiguration` with the additional `EntityLinkRepository` parameter.

### Aniannoth-overview field naming

The API exposes resolved links as `links: LinkedEntityResponse[]` on every entity response, e.g.:

```json
{ "type": "CHARACTER", "id": "...", "name": "Aroneus", "status": "CANON" }
```

The FE should render this list as clickable cross-references (see TODO.md / FE spec for aniannoth-overview).

---

*Maintained by Imaws. Update whenever a new entity is wired into entity_links, or the link schema changes.*
