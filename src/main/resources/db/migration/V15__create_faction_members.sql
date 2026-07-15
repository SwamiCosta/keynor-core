-- Adds a faction_members join table to model an ordered roster of
-- Character ids belonging to a Faction (Faction.members, PR #70).
--
-- Distinct from the generic entity_links table (V7): entity_links models
-- an unordered, polymorphic "this entity mentions/references that entity"
-- relationship across all 6 universe entity types, with no real foreign
-- keys. Faction membership is a narrower, strongly-typed, order-preserving
-- relationship between exactly two entity types (Faction -> Character), so
-- it gets real foreign keys and an explicit ordering column instead of
-- reusing the polymorphic table.
--
-- display_order is managed by Hibernate's @OrderColumn (FactionEntity.members)
-- as the list index on every write; the DEFAULT 0 only covers a row
-- inserted outside the ORM's own path and is never relied upon by the
-- application itself.
--
-- Composite primary key mirrors the faction_categories pattern (V2): it
-- both indexes lookups by faction_id and enforces, at the database level,
-- that a character cannot be listed twice as a member of the same faction.
-- No separate index is added, for the same reason faction_categories has
-- none — the composite key's leading column already serves faction_id-only
-- lookups.

CREATE TABLE faction_members (
    faction_id    UUID    NOT NULL REFERENCES factions(id) ON DELETE CASCADE,
    character_id  UUID    NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (faction_id, character_id)
);
