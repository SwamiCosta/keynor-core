CREATE TABLE eras (
    id          VARCHAR(100) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    era_order   INTEGER      NOT NULL,
    period      VARCHAR(200),
    summary     TEXT,
    map_type    VARCHAR(20)  NOT NULL,
    default_map VARCHAR(100),
    color       VARCHAR(20),
    PRIMARY KEY (id)
);

CREATE TABLE maps (
    id       VARCHAR(100) NOT NULL,
    name     VARCHAR(200) NOT NULL,
    map_type VARCHAR(20)  NOT NULL,
    image    TEXT,
    PRIMARY KEY (id)
);

CREATE TABLE map_eras (
    map_id VARCHAR(100) NOT NULL REFERENCES maps(id) ON DELETE CASCADE,
    era_id VARCHAR(100) NOT NULL REFERENCES eras(id) ON DELETE CASCADE,
    PRIMARY KEY (map_id, era_id)
);
