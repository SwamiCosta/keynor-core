# Skill: Hidden Content Implementation

> Covers the `hidden` visibility flag and `hidden_content_lock` table — schema, domain model, and the wiring pattern for the Hidden Content & Black Pins feature. Read before touching anything related to hidden content, or before replicating the pattern to a new entity type. See root `ARCHITECTURE.md` — "Cross-Project Feature: Hidden Content & Black Pins" for the cross-project design.

---

## Concept

`hidden` is a visibility dimension on `UniverseEntity`, orthogonal to `status`. A hidden entity is always `status = CANON` (enforced in `UniverseEntity`'s constructor and by a `CHECK` constraint per table, V17) but is excluded from every public list/browse endpoint regardless. The only routes to it in the UI are a black pin (an existing `MapPin` whose target is hidden) or a link from another already-unlocked hidden entity.

A hidden entity has an associated `HiddenContentLock` (`hidden_content_lock` table, V17): a riddle text and a BCrypt password hash, one row per `(entity_type, entity_id)`. Authored by Aroneus through the normal content API, not hardcoded — see "Aroneus's role" below.

## Current rollout status

**Fully wired:** `Character`, `Lore` — domain, JPA entity/mapper, `Create*Request`/`*Response` DTOs, `Internal*Controller`.

**Schema-only (not yet replicated):** `Place`, `Faction`, `Item`, `Event` — `UniverseEntity.hidden` applies to them too (they extend the same base class), but their JPA entities have no `hidden` column and their `Create*Request`/`*Response` DTOs have no `hidden` field, so `hidden` is always constructed as `false` for these four. `UniverseEntityLookupJpaAdapter.findSummary` passes a literal `false` for these four types' `EntityLinkSummary.hidden()` for the same reason. This is not a bug — it means these four entity types cannot be marked hidden yet, which is correct until replicated.

## Reference implementation: Lore and Character

Both were wired identically. The five changes, per entity:

1. **Domain model** (`domain/model/<entity>/<Entity>.java`) — constructor gains a trailing `boolean hidden` parameter, passed straight through to `super(...)`.
2. **JPA entity** (`infrastructure/persistence/<entity>/<Entity>Entity.java`) — new `@Column(nullable = false) private boolean hidden;` + getter/setter.
3. **Mapper** (`infrastructure/persistence/<entity>/<Entity>Mapper.java`) — `toDomain`/`toEntity` both carry `hidden` across.
4. **Create command + DTOs** — `Create<Entity>UseCase.Command` and `Create<Entity>Request` both gain trailing `boolean hidden, String riddleText, String password`. `Internal<Entity>Controller.create()` passes these through unchanged.
5. **Service** (`domain/service/<Entity>Service.java`) — constructor gains two dependencies: `UniverseEntityLookupRepository` and `CreateHiddenContentLockUseCase`. In `create()`/`update()`, call `HiddenLinkDirectionValidator.validate(sourceHidden, links, universeEntityLookupRepository)` **before** `entityLinkRepository.replaceLinks(...)`. In `create()` only, if `saved.isHidden()`, call `createHiddenContentLockUseCase.createOrReplace(EntityType.X, saved.getId(), command.riddleText(), command.password())`.

`hidden` is **create-only** by design — there is no way to flip it after creation (mirrors how `status` also has no field on `Update*Request`; a dedicated endpoint would be needed to change it later, not built yet).

## Replicating to Place, Faction, Item, Event

Apply the same five changes listed above. Additionally:

- `EntityFilter.excludeHidden` already exists and is already passed by every `Public*Controller` (`true`) and `Internal*Controller` (`false`) for all six entity types — no change needed there.
- Add the `hidden = false` predicate to the entity's `*Specifications.fromFilter` (see `CharacterSpecifications`/`LoreSpecifications` for the pattern: `if (filter.excludeHidden()) { spec = spec.and(...); }`) — this is currently absent for Place/Faction/Item/Event because their JPA entities have no `hidden` column yet; adding the column (step 2 above) makes this predicate buildable.
- Update `UniverseEntityLookupJpaAdapter.findSummary`'s switch case for that type to call `e.isHidden()` instead of the literal `false`.
- Register the new `Service` constructor dependencies in `DomainConfiguration`'s corresponding `@Bean` method.
- Update this skill file's "Current rollout status" section to move the entity from "Schema-only" to "Fully wired".

## Shared infrastructure (already generic, no replication needed)

- `HiddenContentLock` / `HiddenContentLockRepository` / `HiddenContentLockJpaAdapter` — keyed by `(EntityType, UUID)`, works for any of the 6 types already.
- `HiddenContentService` (`domain/service/`) — implements `CreateHiddenContentLockUseCase` and `HiddenContentAccessUseCase` (unlock, hasAccess, findRiddle). Holds the master-password placeholder (`MASTER_PASSWORD` constant — **must be replaced with a real value before deploy**, see the class Javadoc; this is a value only the user may supply, not something an agent should invent, per workspace `SKILLS.md` Skill 14).
- `HmacHiddenUnlockTokenSigner` (`infrastructure/security/`) — stateless token, in-memory HMAC key generated at boot (a restart invalidates all outstanding tokens), 2-hour TTL.
- `BCryptPasswordHasher` (`infrastructure/security/`) — hashes/verifies each hidden entity's own password.
- `PublicHiddenContentController` (`infrastructure/web/hidden/`) — `GET .../riddle`, `POST .../unlock`, `GET .../{entityType}/{entityId}`. The `findById`-equivalent branch switches on `EntityType` and must gain a `case` per newly-replicated entity type, delegating to that entity's own `Find<Entity>ByIdUseCase` and `<Entity>Response.from(...)` (reuse, don't duplicate the response shape).
- `LinkedEntityResponse.from(EntityLinkSummary)` — redacts `name`/`status` unconditionally whenever `summary.hidden()` is true. This is what keeps a black pin (`MapPinResponse`, which embeds a `LinkedEntityResponse`) and every entity's resolved `links` field from leaking a hidden target's identity before it's unlocked. No per-entity change needed here — it already applies to whichever types report `hidden: true` via `EntityLinkSummary`.
- `HiddenLinkDirectionValidator` (`domain/service/`) — the one-way rule (hidden → visible allowed, visible → hidden rejected). Static, stateless, takes any `UniverseEntityLookupRepository`; nothing to change here per entity type, only the call site in each `Service`.

## Aroneus's role — the one-way linking rule

**A hidden entity may link to a visible one. A visible entity may never link to a hidden one.** This is enforced server-side (`HiddenLinkDirectionValidator`, rejected with `HiddenContentLinkViolationException` → `400`), but Aroneus must treat it as a hard authoring rule, not just something the API happens to reject after the fact — see `aroneus.md` for the authoring-facing explanation and rationale.

---

*Maintained by Imaws. Update the "Current rollout status" section whenever a new entity type is replicated.*
