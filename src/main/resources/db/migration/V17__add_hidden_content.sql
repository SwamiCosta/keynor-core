-- Adds "hidden" visibility to the 6 UniverseEntity tables and a table for
-- per-entity riddle/password locks, powering the Hidden Content & Black
-- Pins cross-project feature (see ../../../../../ARCHITECTURE.md).
--
-- `hidden` is a visibility dimension orthogonal to `status` -- a hidden
-- entity is always CANON but is excluded from every public list/browse
-- query regardless of status. Non-destructive, additive-only, per
-- workspace SKILLS.md Skill 02: a literal default (false) covers every
-- existing row in the same statement, so no separate backfill step is
-- needed.
--
-- The CHECK constraint enforces the "hidden content is always canon" rule
-- at the schema level: hidden = true is only valid when status = 'CANON'.

ALTER TABLE characters ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE places     ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE factions    ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE items       ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE events      ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE lore        ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE characters ADD CONSTRAINT chk_characters_hidden_requires_canon CHECK (NOT hidden OR status = 'CANON');
ALTER TABLE places     ADD CONSTRAINT chk_places_hidden_requires_canon     CHECK (NOT hidden OR status = 'CANON');
ALTER TABLE factions    ADD CONSTRAINT chk_factions_hidden_requires_canon  CHECK (NOT hidden OR status = 'CANON');
ALTER TABLE items       ADD CONSTRAINT chk_items_hidden_requires_canon     CHECK (NOT hidden OR status = 'CANON');
ALTER TABLE events      ADD CONSTRAINT chk_events_hidden_requires_canon    CHECK (NOT hidden OR status = 'CANON');
ALTER TABLE lore        ADD CONSTRAINT chk_lore_hidden_requires_canon      CHECK (NOT hidden OR status = 'CANON');

-- One riddle/password lock per hidden entity. Follows the entity_links
-- (V7) / map_pins (V16) convention: entity_type/entity_id are polymorphic,
-- no real FK to the six entity tables, referential integrity enforced in
-- the domain layer since a lock must be able to reference any of the 6
-- entity types.
--
-- password_hash stores a BCrypt hash, not plaintext -- riddle answers are
-- authored by Aroneus through the same content API as any other field,
-- and db/seed/universe-content.sql (git-tracked) would otherwise make the
-- answers trivially readable in the repository, defeating the puzzle for
-- anyone who looks at the seed file or git history.
CREATE TABLE hidden_content_lock (
    entity_type   VARCHAR(20)  NOT NULL,
    entity_id     UUID         NOT NULL,
    riddle_text   TEXT         NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (entity_type, entity_id)
);
