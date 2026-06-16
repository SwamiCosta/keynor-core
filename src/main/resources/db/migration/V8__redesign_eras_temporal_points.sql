-- V8: Redesign eras table to support temporal points alongside era intervals.
--
-- The old eras table (V4) used string IDs and Phase-1 fields (period, map_type,
-- default_map, color). This migration replaces it with a clean UUID-keyed schema
-- that introduces a type discriminator (ERA vs POINT) and an importance field
-- for temporal point entries.
--
-- map_eras.era_id had a FOREIGN KEY to eras(id). We drop that constraint before
-- dropping eras so the maps module is not broken. The map_eras table itself is
-- kept as-is; its era_id column becomes a loose reference with no DB-level FK.

-- 1. Drop the foreign key from map_eras to eras (constraint defined in V4).
ALTER TABLE map_eras DROP CONSTRAINT IF EXISTS map_eras_era_id_fkey;

-- 2. Drop the old eras table.
DROP TABLE IF EXISTS eras;

-- 3. Create the new eras table with temporal point support.
CREATE TABLE eras (
    id          UUID         PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    order_index INTEGER      NOT NULL,
    type        VARCHAR(10)  NOT NULL,
    importance  VARCHAR(10),
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 4. Index on order_index for ordered list queries.
CREATE INDEX idx_eras_order_index ON eras (order_index);
