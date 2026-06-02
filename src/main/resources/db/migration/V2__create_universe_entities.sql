-- Characters
CREATE TABLE characters (
    id                  UUID        PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    summary             TEXT,
    body                TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    timeline_founded    VARCHAR(255),
    timeline_destroyed  VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

CREATE TABLE character_categories (
    character_id UUID        NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    category     VARCHAR(50) NOT NULL,
    PRIMARY KEY (character_id, category)
);

CREATE TABLE character_tags (
    character_id UUID         NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    tag          VARCHAR(100) NOT NULL,
    PRIMARY KEY (character_id, tag)
);

-- Places
CREATE TABLE places (
    id                  UUID        PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    summary             TEXT,
    body                TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    map_type            VARCHAR(20),
    timeline_founded    VARCHAR(255),
    timeline_destroyed  VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

CREATE TABLE place_categories (
    place_id UUID        NOT NULL REFERENCES places(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    PRIMARY KEY (place_id, category)
);

CREATE TABLE place_tags (
    place_id UUID         NOT NULL REFERENCES places(id) ON DELETE CASCADE,
    tag      VARCHAR(100) NOT NULL,
    PRIMARY KEY (place_id, tag)
);

-- Factions
CREATE TABLE factions (
    id                  UUID        PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    summary             TEXT,
    body                TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    timeline_founded    VARCHAR(255),
    timeline_destroyed  VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

CREATE TABLE faction_categories (
    faction_id UUID        NOT NULL REFERENCES factions(id) ON DELETE CASCADE,
    category   VARCHAR(50) NOT NULL,
    PRIMARY KEY (faction_id, category)
);

CREATE TABLE faction_tags (
    faction_id UUID         NOT NULL REFERENCES factions(id) ON DELETE CASCADE,
    tag        VARCHAR(100) NOT NULL,
    PRIMARY KEY (faction_id, tag)
);

-- Items
CREATE TABLE items (
    id                  UUID        PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    summary             TEXT,
    body                TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    timeline_founded    VARCHAR(255),
    timeline_destroyed  VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

CREATE TABLE item_categories (
    item_id  UUID        NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    PRIMARY KEY (item_id, category)
);

CREATE TABLE item_tags (
    item_id UUID         NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    tag     VARCHAR(100) NOT NULL,
    PRIMARY KEY (item_id, tag)
);

-- Events
CREATE TABLE events (
    id                  UUID        PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    summary             TEXT,
    body                TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    timeline_founded    VARCHAR(255),
    timeline_destroyed  VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

CREATE TABLE event_categories (
    event_id UUID        NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    PRIMARY KEY (event_id, category)
);

CREATE TABLE event_tags (
    event_id UUID         NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    tag      VARCHAR(100) NOT NULL,
    PRIMARY KEY (event_id, tag)
);

-- Lore
CREATE TABLE lore (
    id                  UUID        PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    summary             TEXT,
    body                TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    timeline_founded    VARCHAR(255),
    timeline_destroyed  VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

CREATE TABLE lore_categories (
    lore_id  UUID        NOT NULL REFERENCES lore(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    PRIMARY KEY (lore_id, category)
);

CREATE TABLE lore_tags (
    lore_id UUID         NOT NULL REFERENCES lore(id) ON DELETE CASCADE,
    tag     VARCHAR(100) NOT NULL,
    PRIMARY KEY (lore_id, tag)
);
