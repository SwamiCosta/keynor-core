-- Drops the tags concept entirely. Tags were modeled as one many-to-many
-- join table per universe entity type (introduced in
-- V2__create_universe_entities.sql) but never grew beyond a free-text label
-- list with no filtering UI in any consuming client — the feature added
-- noise without payoff, so it is being removed rather than reworked.
--
-- AUTHORIZATION REQUIRED before this runs — merging this migration
-- auto-applies it on the next application boot (Flyway). The 6 DROP TABLE
-- statements below were named individually and authorized by the user on
-- 2026-06-25 before being written, per workspace SKILLS.md — Skill 02.
--
-- Data loss: 13 rows total (2 characters, 6 lore entries) — already
-- reflected in db/seed/universe-content.sql, which drops the same tables
-- from its TRUNCATE block and removes the corresponding INSERT statements
-- in this same PR. places, factions, items, and events have no tag rows.

DROP TABLE character_tags;
DROP TABLE place_tags;
DROP TABLE faction_tags;
DROP TABLE item_tags;
DROP TABLE event_tags;
DROP TABLE lore_tags;
