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

For the full procedure (authorization gate, destructive-operations rules, Flyway-merge-is-execution warning), see the workspace `SKILLS.md` — Skill 02.

---

*Maintained by Imaws. Update with every new migration version — append a row, never edit history for already-applied versions.*
