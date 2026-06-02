package com.keynor.core.infrastructure.persistence.map;

import com.keynor.core.domain.model.map.GameMap;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class MapMapper {

    public GameMap toDomain(MapEntity entity) {
        return new GameMap(
                entity.getId(),
                entity.getName(),
                entity.getMapType(),
                entity.getImage(),
                entity.getEraIds() != null ? new ArrayList<>(entity.getEraIds()) : new ArrayList<>());
    }
}
