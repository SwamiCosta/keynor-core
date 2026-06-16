CREATE TABLE entity_links (
    id          UUID        PRIMARY KEY,
    source_type VARCHAR(20) NOT NULL,
    source_id   UUID        NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id   UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_entity_link UNIQUE (source_type, source_id, target_type, target_id),
    CONSTRAINT chk_no_self_link CHECK (NOT (source_type = target_type AND source_id = target_id))
);

CREATE INDEX idx_entity_links_source ON entity_links (source_type, source_id);
CREATE INDEX idx_entity_links_target ON entity_links (target_type, target_id);
