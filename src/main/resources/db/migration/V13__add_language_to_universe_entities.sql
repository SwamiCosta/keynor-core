-- Adds multilingual support (EN/PT) to the 6 UniverseEntity tables.
--
-- `language` identifies which language this specific row's text is written
-- in. Every existing row is English content, so it defaults to 'en' and is
-- backfilled that way — non-destructive, additive-only, per workspace
-- SKILLS.md Skill 02.
--
-- `translation_group_id` links an EN row to its PT counterpart (and vice
-- versa) without privileging either language as "the original" — it is a
-- shared group id, not a one-directional FK. For existing rows (which have
-- no translation yet), each row is backfilled as its own group anchor
-- (translation_group_id = id). When a translation is later added — via
-- Aroneus's normal creation flow supplying an explicit translationGroupId,
-- or via Siegmund's one-time historical duplication — the new row joins
-- the same group instead of starting a new one. This is what lets Aroneus/
-- Siegmund/Lethra detect "this entity has no translation yet" going
-- forward (group has fewer distinct languages than the 2 supported).
--
-- Non-destructive path (Skill 02): add nullable, backfill, then tighten to
-- NOT NULL — applies to translation_group_id, which has no static default.
-- `language` gets its NOT NULL directly since a literal default ('en')
-- covers every existing row in the same statement.

ALTER TABLE characters ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'pt'));
ALTER TABLE places     ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'pt'));
ALTER TABLE factions    ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'pt'));
ALTER TABLE items       ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'pt'));
ALTER TABLE events      ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'pt'));
ALTER TABLE lore        ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'pt'));

ALTER TABLE characters ADD COLUMN translation_group_id UUID;
ALTER TABLE places     ADD COLUMN translation_group_id UUID;
ALTER TABLE factions    ADD COLUMN translation_group_id UUID;
ALTER TABLE items       ADD COLUMN translation_group_id UUID;
ALTER TABLE events      ADD COLUMN translation_group_id UUID;
ALTER TABLE lore        ADD COLUMN translation_group_id UUID;

UPDATE characters SET translation_group_id = id WHERE translation_group_id IS NULL;
UPDATE places     SET translation_group_id = id WHERE translation_group_id IS NULL;
UPDATE factions    SET translation_group_id = id WHERE translation_group_id IS NULL;
UPDATE items       SET translation_group_id = id WHERE translation_group_id IS NULL;
UPDATE events      SET translation_group_id = id WHERE translation_group_id IS NULL;
UPDATE lore        SET translation_group_id = id WHERE translation_group_id IS NULL;

ALTER TABLE characters ALTER COLUMN translation_group_id SET NOT NULL;
ALTER TABLE places     ALTER COLUMN translation_group_id SET NOT NULL;
ALTER TABLE factions    ALTER COLUMN translation_group_id SET NOT NULL;
ALTER TABLE items       ALTER COLUMN translation_group_id SET NOT NULL;
ALTER TABLE events      ALTER COLUMN translation_group_id SET NOT NULL;
ALTER TABLE lore        ALTER COLUMN translation_group_id SET NOT NULL;
