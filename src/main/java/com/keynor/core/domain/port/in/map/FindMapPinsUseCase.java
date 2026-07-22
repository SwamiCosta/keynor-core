package com.keynor.core.domain.port.in.map;

import com.keynor.core.domain.model.map.MapPin;

import java.util.List;

public interface FindMapPinsUseCase {
    List<MapPin> findByMapId(String mapId);
}
