-- Additive only. Does not touch or drop the existing timeline_founded/timeline_destroyed
-- VARCHAR columns — those are repointed by application code in this same change, and
-- dropped later in a separate, separately-authorized migration once verified in production.
-- See .claude/skills/migration-history.md and workspace SKILLS.md Skill 02
-- ("Primary key format changes — value-dependency scan").

ALTER TABLE characters ADD COLUMN timeline_founded_era_id   UUID REFERENCES eras(id);
ALTER TABLE characters ADD COLUMN timeline_destroyed_era_id UUID REFERENCES eras(id);

ALTER TABLE places     ADD COLUMN timeline_founded_era_id   UUID REFERENCES eras(id);
ALTER TABLE places     ADD COLUMN timeline_destroyed_era_id UUID REFERENCES eras(id);

ALTER TABLE factions   ADD COLUMN timeline_founded_era_id   UUID REFERENCES eras(id);
ALTER TABLE factions   ADD COLUMN timeline_destroyed_era_id UUID REFERENCES eras(id);

ALTER TABLE items      ADD COLUMN timeline_founded_era_id   UUID REFERENCES eras(id);
ALTER TABLE items      ADD COLUMN timeline_destroyed_era_id UUID REFERENCES eras(id);

ALTER TABLE events     ADD COLUMN timeline_founded_era_id   UUID REFERENCES eras(id);
ALTER TABLE events     ADD COLUMN timeline_destroyed_era_id UUID REFERENCES eras(id);

ALTER TABLE lore       ADD COLUMN timeline_founded_era_id   UUID REFERENCES eras(id);
ALTER TABLE lore       ADD COLUMN timeline_destroyed_era_id UUID REFERENCES eras(id);
