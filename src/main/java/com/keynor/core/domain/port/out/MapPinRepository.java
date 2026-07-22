package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.shared.EntityType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MapPinRepository {
    List<MapPin> findByMapId(String mapId);
    Optional<MapPin> findById(UUID id);
    boolean existsByMapIdAndEntityTypeAndEntityId(String mapId, EntityType entityType, UUID entityId);
    MapPin save(MapPin pin);
    void deleteById(UUID id);
}
