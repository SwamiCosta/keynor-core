package com.keynor.core.domain.port.in.map;

import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.map.PinShape;
import com.keynor.core.domain.model.shared.EntityType;

import java.util.UUID;

public interface UpdateMapPinUseCase {

    MapPin update(String mapId, UUID pinId, Command command);

    /**
     * {@code entityType}/{@code entityId} null together means "leave the
     * current link untouched" -- see MapPinService. {@code shape} null means
     * "leave the current shape untouched", same rule as {@code name}.
     */
    record Command(double normalizedX, double normalizedY, String name, EntityType entityType, UUID entityId, PinShape shape) {
    }
}
