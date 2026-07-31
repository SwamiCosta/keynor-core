-- Adds entity versioning support to the 6 UniverseEntity tables.
--
-- `version_group_id` links separate entity rows that represent successive
-- "versions" of the same narrative element — e.g. a Character authored as
-- "John Silver" in one era, later followed by a distinct row (own id, own
-- era, own body/summary text) continuing his story in a later era. Both
-- rows keep the same display `name`; this column is what ties them
-- together for backend querying (e.g. "the complete history of John
-- Silver"), separate from the optional, visual `entity_links` relation an
-- author may additionally choose to create between the two rows.
--
-- Same shape as `translation_group_id` (V13): a shared group id, not a
-- one-directional "original" FK. For existing rows (which have no other
-- version yet), each row is backfilled as its own group anchor
-- (version_group_id = id). A later version joins the same group by
-- supplying the earlier version's own id as versionGroupId on creation.
--
-- Deliberately independent of `hidden`, `status`, and `language` — a
-- version group may freely mix hidden and non-hidden rows, any status,
-- any language. No CHECK constraint enforces homogeneity across a group.
--
-- Non-destructive path (Skill 02): add nullable, backfill, then tighten to
-- NOT NULL — same sequence used for translation_group_id in V13.

ALTER TABLE characters ADD COLUMN version_group_id UUID;
ALTER TABLE places     ADD COLUMN version_group_id UUID;
ALTER TABLE factions   ADD COLUMN version_group_id UUID;
ALTER TABLE items      ADD COLUMN version_group_id UUID;
ALTER TABLE events     ADD COLUMN version_group_id UUID;
ALTER TABLE lore       ADD COLUMN version_group_id UUID;

UPDATE characters SET version_group_id = id WHERE version_group_id IS NULL;
UPDATE places     SET version_group_id = id WHERE version_group_id IS NULL;
UPDATE factions   SET version_group_id = id WHERE version_group_id IS NULL;
UPDATE items      SET version_group_id = id WHERE version_group_id IS NULL;
UPDATE events     SET version_group_id = id WHERE version_group_id IS NULL;
UPDATE lore       SET version_group_id = id WHERE version_group_id IS NULL;

ALTER TABLE characters ALTER COLUMN version_group_id SET NOT NULL;
ALTER TABLE places     ALTER COLUMN version_group_id SET NOT NULL;
ALTER TABLE factions   ALTER COLUMN version_group_id SET NOT NULL;
ALTER TABLE items      ALTER COLUMN version_group_id SET NOT NULL;
ALTER TABLE events     ALTER COLUMN version_group_id SET NOT NULL;
ALTER TABLE lore       ALTER COLUMN version_group_id SET NOT NULL;
