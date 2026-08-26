package com.keynor.core.infrastructure.persistence.map;

import com.keynor.core.domain.model.map.MapPin;
import org.springframework.stereotype.Component;

@Component
public class MapPinMapper {

    public MapPin toDomain(MapPinEntity entity) {
        return new MapPin(
                entity.getId(),
                entity.getMapId(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getName(),
                entity.getShape(),
                entity.getNormalizedX(),
                entity.getNormalizedY(),
                entity.getCreatedAt());
    }

    public MapPinEntity toEntity(MapPin pin) {
        MapPinEntity entity = new MapPinEntity();
        entity.setId(pin.getId());
        entity.setMapId(pin.getMapId());
        entity.setEntityType(pin.getEntityType());
        entity.setEntityId(pin.getEntityId());
        entity.setName(pin.getName());
        entity.setShape(pin.getShape());
        entity.setNormalizedX(pin.getNormalizedX());
        entity.setNormalizedY(pin.getNormalizedY());
        entity.setCreatedAt(pin.getCreatedAt());
        return entity;
    }
}
