package com.keynor.core.domain.port.in.map;

import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.shared.EntityType;

import java.util.UUID;

public interface UpdateMapPinUseCase {

    MapPin update(String mapId, UUID pinId, Command command);

    /**
     * {@code entityType}/{@code entityId} null together means "leave the
     * current link untouched" -- see MapPinService.
     */
    record Command(double normalizedX, double normalizedY, String name, EntityType entityType, UUID entityId) {
    }
}
