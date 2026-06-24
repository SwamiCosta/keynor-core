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

For the full procedure (authorization gate, destructive-operations rules, Flyway-merge-is-execution warning), see the workspace `SKILLS.md` — Skill 02.

---

*Maintained by Imaws. Update with every new migration version — append a row, never edit history for already-applied versions.*
