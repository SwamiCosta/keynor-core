# Skill — Universe Content Dump

**Scope:** keynor-core. Applies whenever universe content data needs to be versioned, applied to a new environment, or inspected for divergence.

---

## What this is

`db/seed/universe-content.sql` is the single source of truth for all universe content data. It replaces the previous incremental seed script model. Instead of creating new seed files for every data change, this file is regenerated to reflect the complete current state of the universe tables.

---

## Scope of tables

The dump covers all universe content tables — entities and their join tables:

```
maps, map_eras, eras,
characters, character_categories,
places, place_categories,
factions, faction_categories, faction_members,
items, item_categories,
events, event_categories,
lore, lore_categories,
universe_entity_images, entity_links,
archetypes, signs, map_pins, hidden_content_lock
```

**Excluded tables** (never in scope):
- `users` — environment-specific, contains hashed credentials
- `oauth2_registered_client`, `oauth2_authorization`, `oauth2_authorization_consent` — environment-specific secrets
- Any future table that receives input from external users

**Resolved** (2026-07-30): `map_pins` (V16, map-pins feature) is now in scope, brought in by Omnia's decision once it held real data (13 rows). It's structurally identical to `entity_links` — inputter-authored but universe-descriptive, not environment-specific — so it's treated the same way. The `MAP_PINS` section sits after `SIGNS` in the file, since `maps` and every entity type it can reference are already inserted earlier in file order.

**Resolved** (2026-08-21): `hidden_content_lock` (V17) is now in scope, per explicit user decision — reversing V17's own original warning against leaking riddle answers into git history. The user's stated rationale: these riddle/password pairs are a gamification detail (an easter-egg puzzle mechanic), not real user credentials protecting any real asset, and carry no security consequence if committed and visible in the repository. **This is a formal, documented exception to the workspace's normal "never commit anything password-shaped" instinct — not an oversight.** `password_hash` still stores a BCrypt hash (schema unchanged, `V17__add_hidden_content.sql` untouched), computed the same way as the bootstrap admin/SYSTEM-client hashes (`security-model.md` — Step 1, jshell + `BCryptPasswordEncoder`); hashing still happens, it's just no longer treated as a reason to exclude the table from the dump. The `HIDDEN_CONTENT_LOCK` section sits after `MAP_PINS` in the file, since it has no ordering dependency on any other table beyond the six entity tables already inserted earlier.

If a new table is added to the schema, the agent must evaluate whether it is universe content or user/environment data before adding it to the dump scope. When in doubt, exclude and flag to Imaws.

---

## What agents may do

- **Read** the current `universe-content.sql` file
- **Generate** a new version of `universe-content.sql` based on known data (construct the SQL manually or from pg_dump output)
- **Inspect** the current DB state via `SELECT` queries (100-row limit, read-only) to diagnose divergence
- **Run `pg_dump` directly** against the already-running local database — standing exception to the SELECT-row-limit rule, scoped strictly to: `--data-only --column-inserts`, limited to the table list in "Scope of tables" above, against the DB instance the user already has running. Never used to start, stop, or restore a database, and never run against anything other than the local dev instance. If the database is not reachable, Siegmund stops and reports instead of starting one (see `CLAUDE.md` — Local environment assumptions)
- **Commit and push** the updated file to a `task/*` branch and open a PR

## What agents may never do

- **Execute** any SQL from this file against any database (INSERT, UPDATE, DELETE, TRUNCATE are all protected actions)
- **Apply** the dump to a new environment — the user does this manually
- **Start, stop, or restart** the database to perform a dump — see `CLAUDE.md` — Local environment assumptions

---

## File format

The file opens with a `TRUNCATE ... CASCADE` block, followed by `INSERT` statements in foreign key dependency order. This ensures the file is a complete replacement — not an additive patch — when applied to an already-seeded database.

Each table — including join/category tables — gets its own contiguous section: a comment divider, then `pg_dump`'s own `--column-inserts` output for that table pasted **verbatim**, one full `INSERT` statement per row.

**Do not**:
- Consolidate rows into multi-row `INSERT ... VALUES (...), (...), ...;` lists. One statement per row keeps every row diff-local — adding or removing a row never touches a neighboring line's trailing comma/semicolon.
- Interleave a join table's rows under its parent entity's row (e.g. `character_categories` rows directly after each character). Every table is its own section, full stop — there is no per-table exception.
- Hand-edit escaping (`E'...'`, quoting) on any row. `pg_dump` already emits correct escaping; retyping it is how mistakes get introduced.

The only hand-authored parts of the file are the header/changelog block and the section divider comments — both are cheap because they don't require touching row data.

```sql
-- universe-content.sql
-- Single source of truth for all universe content data.
-- Generated by Siegmund on request. Applied manually by the user.
-- ⚠ Destructive: TRUNCATE removes all existing universe content before reinserting.
-- ⚠ Apply only after Flyway migrations are fully up to date.
-- ⚠ NOT idempotent via re-run — do not apply twice on the same database.
--
-- Last updated: ...
-- Updated by: ... (one-line description of what changed)

TRUNCATE
    universe_entity_images,
    character_categories,
    lore_categories,
    -- ... all join tables first ...
    characters, lore, places, factions, items, events,
    map_eras, eras, maps
CASCADE;

-- ============================================================
-- MAPS
-- ============================================================
INSERT INTO maps (...) VALUES (...);

-- ============================================================
-- ERAS
-- ============================================================
INSERT INTO eras (...) VALUES (...);
INSERT INTO eras (...) VALUES (...);
-- etc., one INSERT per row, in dependency order
```

---

## Procedure — Update the dump

Triggered when: the user asks Siegmund to update the dump after a data change in the local DB.

1. Siegmund runs `pg_dump --data-only --column-inserts -t maps -t eras -t map_eras -t characters -t character_categories -t places -t place_categories -t factions -t faction_categories -t faction_members -t items -t item_categories -t events -t event_categories -t lore -t lore_categories -t universe_entity_images -t entity_links -t archetypes -t signs -t map_pins -t hidden_content_lock keynor_core` directly against the already-running local database. If the database is not reachable, Siegmund stops and reports — never starts one to proceed
2. Siegmund assembles the file: TRUNCATE block (table list carried over from the previous file), then one section per table in dependency order, each containing that table's `pg_dump` output pasted verbatim — see "File format" above for what NOT to hand-rework
3. Siegmund updates the header's "Last updated" / "Updated by" lines to describe the change
4. Siegmund verifies the new file against the previous version **by row content per table** (e.g. sort both old and new row sets by primary key before diffing) — not by literal line position. Physical row reordering with identical content (Postgres heap relocation after a past `UPDATE`) is expected and is not a defect
5. Siegmund replaces `db/seed/universe-content.sql` with the new content
6. Siegmund commits to a `task/*` branch and opens a PR

---

## Procedure — Diagnose divergence

Triggered when: setting up a new machine, or after suspected data inconsistency.

Siegmund compares the dump with the actual DB state using targeted `SELECT` queries (read-only, 100-row limit):

```sql
-- Check entity counts match expected
SELECT 'maps' AS table_name, COUNT(*) FROM maps
UNION ALL SELECT 'eras', COUNT(*) FROM eras
UNION ALL SELECT 'characters', COUNT(*) FROM characters
UNION ALL SELECT 'lore', COUNT(*) FROM lore
UNION ALL SELECT 'universe_entity_images', COUNT(*) FROM universe_entity_images;

-- Spot-check specific known entities
SELECT id, name, timeline_founded, status FROM characters LIMIT 100;
SELECT id, name, timeline_founded, status FROM lore LIMIT 100;
```

Siegmund reports:
- Which tables have row count mismatches vs the dump
- Which specific entities are missing or differ
- Whether the dump needs to be updated, or the DB needs the dump applied

The user decides which action to take.

---

## Procedure — Apply to a new machine

The user performs all of these steps manually:

1. Clone the repository
2. Start PostgreSQL via Docker Compose: `docker-compose up -d postgres`
3. Start the application once (Flyway runs all migrations automatically)
4. Stop the application
5. Apply the bootstrap SQL — see `security-model.md`'s "Bootstrap and token acquisition" section, every one of these, not a subset:
   - Step 2 — the ADMIN user
   - Step 3 — the `system-client` SYSTEM client
   - Step 4 — **every** currently-documented PKCE client, by name (`rpg-client`, `aniannoth-admin`, and any added since) — a login flow for whichever one gets skipped will look broken in a way that doesn't obviously point back to "this client was never registered" (see that section's own note on the 2026-08-26 incident this caused)
6. Apply the universe content dump:
   ```bash
   psql -U keynor -d keynor_core -f src/main/resources/db/seed/universe-content.sql
   ```
7. Start the application again

---

## Rationale

Incremental seed scripts accumulate over time and become hard to apply correctly on a new machine — the operator must know which scripts to run and in what order. A single dump file always reflects the actual current state and can be applied in one step. Divergence inspection becomes trivial because there is one reference point, not a chain of patches.
