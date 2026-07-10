-- Same multilingual support (EN/PT) added in V13 to the 6 UniverseEntity
-- tables, extended to eras, archetypes, and signs. Separate migration
-- because these three are not UniverseEntity subclasses and needed their
-- own ALTER statements — same non-destructive add/backfill/tighten
-- sequence, same translation_group_id semantics (see V13 for the full
-- rationale).
--
-- eras.name carries a pre-existing UNIQUE constraint (V4/V8). This is not
-- widened to UNIQUE(name, language): a PT era's name is itself translated
-- text (unlike character names, which stay untranslated — see the
-- workspace translation rules), so it will never literally collide with
-- its EN counterpart's name.

ALTER TABLE eras       ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'pt'));
ALTER TABLE archetypes ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'pt'));
ALTER TABLE signs      ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'pt'));

ALTER TABLE eras       ADD COLUMN translation_group_id UUID;
ALTER TABLE archetypes ADD COLUMN translation_group_id UUID;
ALTER TABLE signs      ADD COLUMN translation_group_id UUID;

UPDATE eras       SET translation_group_id = id WHERE translation_group_id IS NULL;
UPDATE archetypes SET translation_group_id = id WHERE translation_group_id IS NULL;
UPDATE signs      SET translation_group_id = id WHERE translation_group_id IS NULL;

ALTER TABLE eras       ALTER COLUMN translation_group_id SET NOT NULL;
ALTER TABLE archetypes ALTER COLUMN translation_group_id SET NOT NULL;
ALTER TABLE signs      ALTER COLUMN translation_group_id SET NOT NULL;
