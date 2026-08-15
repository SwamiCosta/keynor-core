package com.keynor.core.infrastructure.web.map;

import com.keynor.core.domain.model.shared.EntityType;

final class MapPinRequestSupport {

    private MapPinRequestSupport() {
    }

    static EntityType parseEntityType(String entityType) {
        return entityType == null ? null : EntityType.valueOf(entityType.toUpperCase());
    }
}
