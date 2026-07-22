package com.keynor.core.domain.port.in.map;

import com.keynor.core.domain.model.map.MapPin;

import java.util.UUID;

public interface UpdateMapPinUseCase {

    MapPin update(String mapId, UUID pinId, Command command);

    record Command(double normalizedX, double normalizedY) {
    }
}
