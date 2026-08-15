package com.keynor.core.domain.port.in.map;

import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.shared.EntityType;

import java.util.UUID;

public interface CreateMapPinUseCase {

    MapPin create(Command command);

    record Command(String mapId, EntityType entityType, UUID entityId, String name, double normalizedX, double normalizedY) {
    }
}
