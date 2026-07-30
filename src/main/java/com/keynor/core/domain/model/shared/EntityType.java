package com.keynor.core.domain.model.shared;

public enum EntityType {
    CHARACTER,
    PLACE,
    FACTION,
    ITEM,
    EVENT,
    LORE,
    // Not a UniverseEntity subclass (see Era) -- valid only as an entity_links
    // source_type (an era/point description linking out to a real entity).
    // Never a valid target_type -- see UniverseEntityLookupJpaAdapter.
    ERA
}
