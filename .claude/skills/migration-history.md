# Skill: Migration History

> Changelog of all Flyway migrations applied to keynor-core. Read before drafting a new migration, to know the current schema baseline and avoid colliding version numbers.

---

## Database migrations (Flyway)

| Version | Description |
|---------|-------------|
| V1 | `users` table |
| V2 | 6 entity tables + 12 join tables (categories, tags) |
| V3 | Spring Authorization Server OAuth2 tables |
| V4 | `eras` and `maps` tables |
| V5 | `timeline_founded NOT NULL` on all 6 entity tables |
| V6 | `universe_entity_images` table |
| V7 | `entity_links` table (cross-entity references) |
| V8 | Redesign `eras` table — UUID PK, `type` (ERA/POINT), `importance` (STANDARD/MAJOR/null), `order_index`, `description`; drops V4 schema |
| V9 | Add `timeline_founded_era_id` / `timeline_destroyed_era_id` (UUID, FK to `eras.id`) to all 6 entity tables; additive only, legacy `timeline_founded`/`timeline_destroyed` VARCHAR columns untouched |
| V10 | Drop legacy `timeline_founded` / `timeline_destroyed` VARCHAR columns from all 6 entity tables, now superseded by the V9 `*_era_id` columns |
| V11 | `archetypes` and `signs` tables — closed reference/lookup sets for the Aelimic cosmology (5 archetypes, 13 signs including the Rift); `signs.archetype_id` is a `NOT NULL` FK to `archetypes.id` |
| V12 | Drop the tags concept — removes the 6 `*_tags` join tables (`character_tags`, `place_tags`, `faction_tags`, `item_tags`, `event_tags`, `lore_tags`) introduced in V2; the feature never grew beyond a free-text label list with no filtering UI in any consuming client |
| V13 | Add multilingual support (EN/PT) to the 6 `UniverseEntity` tables — `language VARCHAR(2) NOT NULL DEFAULT 'en'` (`CHECK IN ('en','pt')`) and `translation_group_id UUID NOT NULL`, added nullable then backfilled (`translation_group_id = id` for all pre-existing rows) then tightened to `NOT NULL`, per the Skill 02 non-destructive path |
| V14 | Same multilingual support (EN/PT) added in V13, extended to `eras`, `archetypes`, and `signs` — same `language` / `translation_group_id` columns and backfill sequence; separate migration because these three are not `UniverseEntity` subclasses. `eras.name`'s pre-existing `UNIQUE` constraint (V4/V8) is deliberately not widened to `UNIQUE(name, language)` — a PT era's name is itself translated text, so it never literally collides with its EN counterpart |
| V15 | `faction_members` join table — ordered roster of `Character` ids belonging to a `Faction` (`Faction.members`, PR #70); real FKs to `factions.id`/`characters.id` with `ON DELETE CASCADE`, `display_order` managed by Hibernate's `@OrderColumn`, composite `PRIMARY KEY (faction_id, character_id)` mirroring the `faction_categories` (V2) pattern — deliberately not modeled via the polymorphic `entity_links` table (V7), since membership is order-preserving and strongly typed to a single entity pair, unlike `entity_links`'s unordered any-to-any references |
| V16 | `map_pins` table — interactive map pins (aniannoth-overview), linking a normalized `(x, y)` position on a `GameMap` to any universe entity; real FK `map_id → maps.id` (`ON DELETE CASCADE`), but `entity_type`/`entity_id` follow the `entity_links` (V7) convention — polymorphic, no real FK, referential integrity enforced in the domain layer — since coordinates must be able to reference any of the 6 entity types, not just `Place`; `UNIQUE (map_id, entity_type, entity_id)` — one pin per entity per map, but the same entity can appear at different positions on different maps; `normalized_x`/`normalized_y` are `CHECK`-constrained to the `[0, 1]` range (percentage of image width/height, not raw pixels) |
| V17 | Hidden Content & Black Pins feature (see root `ARCHITECTURE.md`). Adds `hidden BOOLEAN NOT NULL DEFAULT false` to all 6 `UniverseEntity` tables, plus a `CHECK` constraint per table (`chk_<table>_hidden_requires_canon`) enforcing `NOT hidden OR status = 'CANON'` — hidden content is always canon. New table `hidden_content_lock (entity_type, entity_id, riddle_text, password_hash, created_at, updated_at)`, `PRIMARY KEY (entity_type, entity_id)`, one row per hidden entity; follows the `entity_links` (V7) / `map_pins` (V16) polymorphic convention — no real FK to the six entity tables. `password_hash` stores a BCrypt hash, not plaintext, since `db/seed/universe-content.sql` is git-tracked and a plaintext answer there would be trivially readable, defeating the riddle |

For the full procedure (authorization gate, destructive-operations rules, Flyway-merge-is-execution warning), see the workspace `SKILLS.md` — Skill 02.

---

*Maintained by Imaws. Update with every new migration version — append a row, never edit history for already-applied versions.*
