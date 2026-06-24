-- V11: Create archetypes and signs tables.
--
-- Archetypes are the four elemental foundations of the Aelimic cosmology
-- (Communication, Might, Cunning, Will) plus the fifth, order-less Obsession,
-- which backs only the thirteenth sign (the Rift). Signs are the twelve
-- seasonal signs plus the Rift, each tied to exactly one archetype.
--
-- Both tables are closed reference/lookup sets — read-only via the public API
-- for now (see PublicArchetypeController / PublicSignController). No create
-- endpoint is introduced in this migration's accompanying code.

CREATE TABLE archetypes (
    id                 UUID         PRIMARY KEY,
    name               VARCHAR(100) NOT NULL UNIQUE,
    element            VARCHAR(50),
    suit               VARCHAR(50),
    vocation           VARCHAR(100),
    temperament        VARCHAR(20),
    cognitive_function VARCHAR(20),
    self_relation      VARCHAR(20),
    description        TEXT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE signs (
    id            UUID         PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    sign_order    INTEGER      NOT NULL,
    season_time   VARCHAR(255),
    archetype_id  UUID         NOT NULL REFERENCES archetypes(id),
    sub_archetype VARCHAR(100),
    summary       TEXT,
    body          TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_signs_order ON signs (sign_order);
CREATE INDEX idx_signs_archetype_id ON signs (archetype_id);
