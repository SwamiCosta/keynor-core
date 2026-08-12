-- Adds "common" content to the 6 UniverseEntity tables.
--
-- `common` marks an entity that never appears in any public browse/list
-- endpoint or on any map pin -- it is reachable only via findById (already
-- unauthenticated/unfiltered today) or as a resolved target inside another
-- entity's `links` field. Unlike `hidden`, there is no riddle/password lock
-- and no redaction: a common entity's name/status render normally wherever
-- it is resolved as a link. See common-content-implementation.md.
--
-- Deliberately carries no CHECK constraint tying it to `status`, unlike
-- `hidden` -- a common entity may be DRAFT, CANON, or DEPRECATED, since
-- nothing about the feature requires it to be canon-only.
--
-- Non-destructive, additive-only, per workspace SKILLS.md Skill 02: a
-- literal default (false) covers every existing row in the same statement,
-- so no separate backfill step is needed.

ALTER TABLE characters ADD COLUMN common BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE places     ADD COLUMN common BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE factions   ADD COLUMN common BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE items      ADD COLUMN common BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE events     ADD COLUMN common BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE lore       ADD COLUMN common BOOLEAN NOT NULL DEFAULT false;
