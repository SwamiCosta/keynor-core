package com.keynor.core.infrastructure.web.map;

import com.keynor.core.domain.model.map.PinShape;
import com.keynor.core.domain.model.shared.EntityType;

final class MapPinRequestSupport {

    private MapPinRequestSupport() {
    }

    static EntityType parseEntityType(String entityType) {
        return entityType == null ? null : EntityType.valueOf(entityType.toUpperCase());
    }

    /**
     * Absent/blank defaults to {@code DEFAULT} -- used on create, where the
     * pin always needs a concrete shape. On update, a null result must be
     * interpreted as "leave the current shape untouched" by the caller
     * instead -- see UpdateMapPinRequest.
     */
    static PinShape parsePinShapeOrDefault(String shape) {
        return (shape == null || shape.isBlank()) ? PinShape.DEFAULT : PinShape.valueOf(shape.toUpperCase());
    }

    /**
     * Null when absent/blank, meaning "leave untouched" -- see
     * UpdateMapPinRequest and MapPinService#update.
     */
    static PinShape parsePinShapeOrNull(String shape) {
        return (shape == null || shape.isBlank()) ? null : PinShape.valueOf(shape.toUpperCase());
    }
}
