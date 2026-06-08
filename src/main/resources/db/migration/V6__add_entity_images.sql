CREATE TABLE universe_entity_images (
    entity_id     UUID          NOT NULL,
    image_url     VARCHAR(2048) NOT NULL,
    display_order INTEGER       NOT NULL DEFAULT 0
);
