package com.keynor.core.application.dto.map;

import com.keynor.core.domain.model.map.GameMap;

import java.util.List;

public record MapResponse(
        String id,
        String name,
        String mapType,
        String image,
        List<String> eraIds) {

    public static MapResponse from(GameMap map) {
        return new MapResponse(
                map.getId(),
                map.getName(),
                map.getMapType().name(),
                map.getImage(),
                map.getEraIds());
    }
}
