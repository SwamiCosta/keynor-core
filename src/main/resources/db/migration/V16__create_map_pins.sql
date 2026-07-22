CREATE TABLE map_pins (
    id            UUID         PRIMARY KEY,
    map_id        VARCHAR(100) NOT NULL REFERENCES maps(id) ON DELETE CASCADE,
    entity_type   VARCHAR(20)  NOT NULL,
    entity_id     UUID         NOT NULL,
    normalized_x  DOUBLE PRECISION NOT NULL,
    normalized_y  DOUBLE PRECISION NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_map_pin UNIQUE (map_id, entity_type, entity_id),
    CONSTRAINT chk_map_pin_normalized_x_range CHECK (normalized_x >= 0 AND normalized_x <= 1),
    CONSTRAINT chk_map_pin_normalized_y_range CHECK (normalized_y >= 0 AND normalized_y <= 1)
);
