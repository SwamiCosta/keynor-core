-- backfill_timeline_era_ids.sql
--
-- Populates timeline_founded_era_id / timeline_destroyed_era_id (added by
-- V9__add_timeline_era_fk_columns.sql) on the 6 universe-entity tables from
-- the legacy free-text timeline_founded / timeline_destroyed VARCHAR columns.
--
-- NOT a Flyway migration. Deliberately kept outside src/main/resources/db/migration
-- so it is never auto-applied on application boot (Flyway auto-applies everything
-- in that directory — see workspace SKILLS.md Skill 02).
--
-- Write-only. Requires explicit user authorization before execution.
-- Agents never run this directly against any database.
--
-- Idempotent: each UPDATE only touches rows where the new *_era_id column is
-- still NULL, so re-running this script after a partial or repeated apply is safe.
--
-- Legacy value -> era name mapping, verified against current data on 2026-06-24:
-- 'primordial' is the only distinct legacy value present in timeline_founded or
-- timeline_destroyed across all 6 entity tables (1 character row, 5 lore rows;
-- timeline_destroyed is NULL on all 6). Add a row to legacy_era_map below if a
-- future audit finds additional legacy values before the legacy VARCHAR columns
-- are dropped (see migration-history.md and Skill 02 — "Primary key format
-- changes — value-dependency scan").
--
-- How to apply:
--   docker exec -i keynor-core-postgres-1 psql -U keynor -d keynor_core -f - < src/main/resources/db/backfill/backfill_timeline_era_ids.sql
--
-- How to verify afterward (expect 0 rows — every legacy value should now be resolved):
--   SELECT 'characters', id, timeline_founded FROM characters WHERE timeline_founded IS NOT NULL AND timeline_founded_era_id IS NULL
--   UNION ALL SELECT 'places', id, timeline_founded FROM places WHERE timeline_founded IS NOT NULL AND timeline_founded_era_id IS NULL
--   UNION ALL SELECT 'factions', id, timeline_founded FROM factions WHERE timeline_founded IS NOT NULL AND timeline_founded_era_id IS NULL
--   UNION ALL SELECT 'items', id, timeline_founded FROM items WHERE timeline_founded IS NOT NULL AND timeline_founded_era_id IS NULL
--   UNION ALL SELECT 'events', id, timeline_founded FROM events WHERE timeline_founded IS NOT NULL AND timeline_founded_era_id IS NULL
--   UNION ALL SELECT 'lore', id, timeline_founded FROM lore WHERE timeline_founded IS NOT NULL AND timeline_founded_era_id IS NULL;

CREATE TEMP TABLE legacy_era_map (legacy_value VARCHAR(255), era_name VARCHAR(255));

INSERT INTO legacy_era_map (legacy_value, era_name) VALUES
    ('primordial', 'Primordial Era');

-- characters
UPDATE characters t SET timeline_founded_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_founded = m.legacy_value AND t.timeline_founded_era_id IS NULL;
UPDATE characters t SET timeline_destroyed_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_destroyed = m.legacy_value AND t.timeline_destroyed_era_id IS NULL;

-- places
UPDATE places t SET timeline_founded_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_founded = m.legacy_value AND t.timeline_founded_era_id IS NULL;
UPDATE places t SET timeline_destroyed_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_destroyed = m.legacy_value AND t.timeline_destroyed_era_id IS NULL;

-- factions
UPDATE factions t SET timeline_founded_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_founded = m.legacy_value AND t.timeline_founded_era_id IS NULL;
UPDATE factions t SET timeline_destroyed_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_destroyed = m.legacy_value AND t.timeline_destroyed_era_id IS NULL;

-- items
UPDATE items t SET timeline_founded_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_founded = m.legacy_value AND t.timeline_founded_era_id IS NULL;
UPDATE items t SET timeline_destroyed_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_destroyed = m.legacy_value AND t.timeline_destroyed_era_id IS NULL;

-- events
UPDATE events t SET timeline_founded_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_founded = m.legacy_value AND t.timeline_founded_era_id IS NULL;
UPDATE events t SET timeline_destroyed_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_destroyed = m.legacy_value AND t.timeline_destroyed_era_id IS NULL;

-- lore
UPDATE lore t SET timeline_founded_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_founded = m.legacy_value AND t.timeline_founded_era_id IS NULL;
UPDATE lore t SET timeline_destroyed_era_id = e.id
    FROM legacy_era_map m JOIN eras e ON e.name = m.era_name
    WHERE t.timeline_destroyed = m.legacy_value AND t.timeline_destroyed_era_id IS NULL;

DROP TABLE legacy_era_map;
