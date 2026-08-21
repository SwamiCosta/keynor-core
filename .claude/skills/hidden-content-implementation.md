# Skill: Hidden Content Implementation

> Covers the `hidden` visibility flag and `hidden_content_lock` table — schema, domain model, and the wiring pattern for the Hidden Content & Black Pins feature. Read before touching anything related to hidden content, or before replicating the pattern to a new entity type. See root `ARCHITECTURE.md` — "Cross-Project Feature: Hidden Content & Black Pins" for the cross-project design.

---

## Concept

`hidden` is a visibility dimension on `UniverseEntity`, orthogonal to `status`. A hidden entity is always `status = CANON` (enforced in `UniverseEntity`'s constructor and by a `CHECK` constraint per table, V17) but is excluded from every public list/browse endpoint regardless. The only routes to it in the UI are a black pin (an existing `MapPin` whose target is hidden) or a link from another already-unlocked hidden entity.

A hidden entity has an associated `HiddenContentLock` (`hidden_content_lock` table, V17): a riddle text and a BCrypt password hash, one row per `(entity_type, entity_id)`. Authored by Aroneus through the normal content API, not hardcoded — see "Aroneus's role" below.

## Current rollout status

**Fully wired — all 6 entity types:** `Character`, `Lore`, `Place`, `Faction`, `Item`, `Event` — domain, JPA entity/mapper, `Create*Request`/`*Response` DTOs, `Internal*Controller`, `*Specifications.excludeHidden`, `UniverseEntityLookupJpaAdapter`, `DomainConfiguration`, and `PublicHiddenContentController`'s dispatch switch. Any universe entity can now be authored as hidden content.

## Reference implementation: Lore and Character

Character and Lore were wired first, then replicated identically to Place, Faction, Item, and Event. The five changes, per entity:

1. **Domain model** (`domain/model/<entity>/<Entity>.java`) — constructor gains a trailing `boolean hidden` parameter, passed straight through to `super(...)`.
2. **JPA entity** (`infrastructure/persistence/<entity>/<Entity>Entity.java`) — new `@Column(nullable = false) private boolean hidden;` + getter/setter.
3. **Mapper** (`infrastructure/persistence/<entity>/<Entity>Mapper.java`) — `toDomain`/`toEntity` both carry `hidden` across.
4. **Create command + DTOs** — `Create<Entity>UseCase.Command` and `Create<Entity>Request` both gain trailing `boolean hidden, String riddleText, String password`. `Internal<Entity>Controller.create()` passes these through unchanged.
5. **Update command + DTOs** — `Update<Entity>UseCase.Command` and `Update<Entity>Request` gain the same trailing `boolean hidden, String riddleText, String password`. `Internal<Entity>Controller.update()` passes them through unchanged.
6. **Service** (`domain/service/<Entity>Service.java`) — constructor gains two dependencies: `UniverseEntityLookupRepository` and `CreateHiddenContentLockUseCase`. In both `create()` and `update()`, call `HiddenLinkDirectionValidator.validate(sourceHidden, links, universeEntityLookupRepository)` **before** `entityLinkRepository.replaceLinks(...)`, and if the result is hidden, call `createHiddenContentLockUseCase.createOrReplace(EntityType.X, saved.getId(), command.riddleText(), command.password())`. `update()` additionally: reject with `IllegalArgumentException` up front if `command.hidden()` is true and `riddleText`/`password` are blank (can't hide something with no way to ever unlock it), and call `entity.setHidden(command.hidden())` before saving.

`hidden` is mutable post-creation via `UniverseEntity.setHidden(boolean)`, which enforces the same "hidden implies CANON" invariant as the constructor. **`Update*Request` is full-replacement** like every other field on it — omitting `hidden` from an update payload defaults to `false` and un-hides the entity. This is a real trap for content authors, not just a technical note — see `aroneus.md`.

## Replicating to a future new entity type

If a 7th universe entity type is ever added, apply the same six changes listed above, plus:

- `EntityFilter.excludeHidden` already exists and is passed by every `Public*Controller` (`true`) and `Internal*Controller` (`false`) — no change needed there.
- Add the `hidden = false` predicate to the entity's `*Specifications.fromFilter` (see any existing `*Specifications` for the pattern: `if (filter.excludeHidden()) { spec = spec.and(...); }`).
- Add a case to `UniverseEntityLookupJpaAdapter.findSummary`'s switch calling `e.isHidden()`.
- Register the new `Service`'s two extra constructor dependencies (`UniverseEntityLookupRepository`, `CreateHiddenContentLockUseCase`) in `DomainConfiguration`.
- Add a case to `PublicHiddenContentController.findById`'s switch, delegating to that entity's own `Find<Entity>ByIdUseCase` and `<Entity>Response.from(...)`.
- Update this skill file's "Current rollout status" section.

## Shared infrastructure (already generic, no replication needed)

- `HiddenContentLock` / `HiddenContentLockRepository` / `HiddenContentLockJpaAdapter` — keyed by `(EntityType, UUID)`, works for any of the 6 types already.
- `HiddenContentService` (`domain/service/`) — implements `CreateHiddenContentLockUseCase` and `HiddenContentAccessUseCase` (unlock, hasAccess, findRiddle). Holds the master password (`MASTER_PASSWORD` constant, compared as plaintext — see the class Javadoc for why hashing it would add nothing) — a value only the user may supply, not something an agent should invent, per workspace `SKILLS.md` Skill 14. Changing it is a normal, user-directed code change like any other, not a one-time bootstrap step.
- `HmacHiddenUnlockTokenSigner` (`infrastructure/security/`) — stateless token, in-memory HMAC key generated at boot (a restart invalidates all outstanding tokens), 2-hour TTL.
- `BCryptPasswordHasher` (`infrastructure/security/`) — hashes/verifies each hidden entity's own password.
- `PublicHiddenContentController` (`infrastructure/web/hidden/`) — `GET .../riddle`, `POST .../unlock`, `GET .../{entityType}/{entityId}`. The `findById`-equivalent branch switches on `EntityType` and must gain a `case` per newly-replicated entity type, delegating to that entity's own `Find<Entity>ByIdUseCase` and `<Entity>Response.from(...)` (reuse, don't duplicate the response shape).
- `LinkedEntityResponse.from(EntityLinkSummary)` — redacts `name`/`status` unconditionally whenever `summary.hidden()` is true. This is what keeps a black pin (`MapPinResponse`, which embeds a `LinkedEntityResponse`) and every entity's resolved `links` field from leaking a hidden target's identity before it's unlocked. No per-entity change needed here — it already applies to whichever types report `hidden: true` via `EntityLinkSummary`.
- `HiddenLinkDirectionValidator` (`domain/service/`) — the one-way rule (hidden → visible allowed, visible → hidden rejected). Static, stateless, takes any `UniverseEntityLookupRepository`; nothing to change here per entity type, only the call site in each `Service`.

## Aroneus's role — the one-way linking rule

**A hidden entity may link to a visible one. A visible entity may never link to a hidden one.** This is enforced server-side (`HiddenLinkDirectionValidator`, rejected with `HiddenContentLinkViolationException` → `400`), but Aroneus must treat it as a hard authoring rule, not just something the API happens to reject after the fact — see `aroneus.md` for the authoring-facing explanation and rationale.

---

*Maintained by Imaws. Update the "Current rollout status" section whenever a new entity type is replicated. When extending a capability to one entity type, extend it to all 6 in the same pass by default — this feature has twice gone through a narrow-then-widened cycle (Create in PR #88→#91, Update in PR #93→#95) that a wider default from the start would have avoided.*
