-- Drops the legacy free-text timeline_founded / timeline_destroyed columns,
-- superseded by timeline_founded_era_id / timeline_destroyed_era_id
-- (see V9__add_timeline_era_fk_columns.sql).
--
-- Safe to run: backfill_timeline_era_ids.sql has already been executed and
-- verified — every row that held a legacy value now has a matching
-- *_era_id value. No data loss.

ALTER TABLE characters DROP COLUMN timeline_founded;
ALTER TABLE characters DROP COLUMN timeline_destroyed;

ALTER TABLE places DROP COLUMN timeline_founded;
ALTER TABLE places DROP COLUMN timeline_destroyed;

ALTER TABLE factions DROP COLUMN timeline_founded;
ALTER TABLE factions DROP COLUMN timeline_destroyed;

ALTER TABLE items DROP COLUMN timeline_founded;
ALTER TABLE items DROP COLUMN timeline_destroyed;

ALTER TABLE events DROP COLUMN timeline_founded;
ALTER TABLE events DROP COLUMN timeline_destroyed;

ALTER TABLE lore DROP COLUMN timeline_founded;
ALTER TABLE lore DROP COLUMN timeline_destroyed;
