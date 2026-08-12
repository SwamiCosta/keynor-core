# Skill: Common Content Implementation

> Covers the `common` visibility flag — schema, domain model, and the wiring pattern for content that is never independently browsable, only reachable via another entity's resolved `links`. Read before touching anything related to common content, or before replicating the pattern to a new entity type.

---

## Concept

`common` is a visibility dimension on `UniverseEntity`, orthogonal to `status` and independent of `hidden` (V19). A common entity:

- Is excluded from every public list/browse endpoint (`GET /api/public/v1/<entity-type>`), the same way `hidden` is
- Is excluded from every map pin — a pin whose target is `common = true` is dropped from `GET /api/public/v1/maps/{mapId}/pins` entirely, not downgraded to a black pin
- **Is reachable via `findById`** (`GET /api/public/v1/<entity-type>/{id}`) — this endpoint was already unfiltered by status/hidden/common before this feature existed, so no change was needed there
- **Is never redacted when resolved as a link** — a common entity's real `name`/`status` render normally wherever it appears inside another entity's `links` field (`LinkedEntityResponse`). This is the entire point: "accessed via references from other elements."

Unlike `hidden`, `common` has **no lock mechanism** — no riddle, no password, no unlock token, no `CreateHiddenContentLockUseCase` equivalent. It is purely a listing/timeline exclusion, not an access-control gate. It also carries **no status invariant** — a common entity may be `DRAFT`, `CANON`, or `DEPRECATED` (unlike `hidden`, which requires `CANON`).

Introduced to support a "common elements" product requirement: some universe entities (any of the 6 types) should never appear in aniannoth-overview's timeline/map/browse UI, existing solely as targets other entities can link to. Since aniannoth-overview holds no local copy of universe data — every view is a direct render of whatever the public API returned — excluding `common` entities from the public list and pin endpoints is sufficient on its own; **no frontend change was required** for this feature (confirmed by Lamont before this was scoped as keynor-core-only work — see the `aniannoth-overview ↔ keynor-core` cross-project activation check that routed this to Imaws).

## Current rollout status

**Fully wired — all 6 entity types:** `Character`, `Place`, `Faction`, `Item`, `Event`, `Lore` — domain, JPA entity/mapper, `Create*Request`/`Update*Request`/`*Response` DTOs, Command records, `Internal*Controller`/`Public*Controller`, `*Specifications.excludeCommon` predicate, `EntityLinkSummary`, `UniverseEntityLookupJpaAdapter`, and `PublicMapPinController`'s pin filter.

## Reference implementation

Wired identically to how `hidden` was wired (see `hidden-content-implementation.md` for the general shape), minus the lock. Per entity:

1. **Domain model** (`domain/model/<entity>/<Entity>.java`) — constructor gains a trailing `boolean common` parameter (after `hidden`), passed straight through to `super(...)`.
2. **JPA entity** (`infrastructure/persistence/<entity>/<Entity>Entity.java`) — new `@Column(nullable = false) private boolean common;` + getter/setter.
3. **Mapper** (`infrastructure/persistence/<entity>/<Entity>Mapper.java`) — `toDomain`/`toEntity` both carry `common` across.
4. **Specifications** (`infrastructure/persistence/<entity>/<Entity>Specifications.java`) — `if (filter.excludeCommon()) spec = spec.and(isNotCommon())`, mirroring the existing `excludeHidden` branch.
5. **Create/Update DTOs and Commands** — `Create<Entity>Request`/`Update<Entity>Request` and their matching `Command` records gain a trailing `boolean common` field. No `riddleText`/`password` — those are `hidden`-only.
6. **Response DTO** (`<Entity>Response`) — gains a trailing `boolean common` field, `entity.isCommon()`.
7. **Service** (`domain/service/<Entity>Service.java`) — `create()` passes `command.common()` into the domain constructor; `update()` calls `entity.setCommon(command.common())`. No new constructor dependencies needed (unlike `hidden`, which needs `UniverseEntityLookupRepository` + `CreateHiddenContentLockUseCase`) — `common` requires no `DomainConfiguration` changes.
8. **Controllers** — `Internal*Controller` passes `request.common()` through to the command on both create and update, and constructs its `EntityFilter` with `excludeCommon = false` (admin/content-management listings still show common entities). `Public*Controller` constructs its `EntityFilter` with `excludeCommon = true`.

`common` is mutable post-creation via `UniverseEntity.setCommon(boolean)`. **`Update*Request` is full-replacement** like every other field on it — omitting `common` from an update payload defaults to `false` and un-marks the entity as common. This is the same trap `hidden` has — see `aroneus.md`.

## Shared infrastructure changes (not per-entity)

- `EntityFilter` gained a trailing `boolean excludeCommon` field, alongside `excludeHidden`.
- `EntityLinkSummary` gained a trailing `boolean common` field, alongside `hidden`.
- `UniverseEntityLookupJpaAdapter.findSummary` populates `common` in all 6 switch cases.
- `PublicCharacterController.findByIds` (the batch-lookup endpoint, the one place outside the adapter that constructs `EntityLinkSummary` manually) also populates `common`.
- `LinkedEntityResponse.from(EntityLinkSummary)` needed **no change** — it only branches on `hidden` for redaction; a common entity's summary passes through unredacted, by design.
- `PublicMapPinController.findByMap` inserts `.filter(summary -> !summary.common())` between resolving the pin's target summary and mapping it to a `MapPinResponse` — a common-flagged target's pin is dropped from the response array entirely.

## Replicating to a future new entity type

If a 7th universe entity type is ever added, apply the 8 reference-implementation steps above. No shared infrastructure changes are needed beyond what already exists — `EntityFilter`, `EntityLinkSummary`, `UniverseEntityLookupJpaAdapter`, and `PublicMapPinController` are already generic across all 6 types.

## Database

Migration V19 (`V19__add_common_flag.sql`) adds `common BOOLEAN NOT NULL DEFAULT false` to all 6 `UniverseEntity` tables — additive-only, no backfill needed (literal default covers existing rows), no CHECK constraint (unlike `hidden`'s canon-only invariant). See `migration-history.md`.

---

*Maintained by Imaws. Update the "Current rollout status" section whenever a new entity type is replicated.*
