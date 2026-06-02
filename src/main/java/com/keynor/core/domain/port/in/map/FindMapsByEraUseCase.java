package com.keynor.core.domain.port.in.map;

import com.keynor.core.domain.model.map.GameMap;

import java.util.List;

public interface FindMapsByEraUseCase {
    List<GameMap> findByEraId(String eraId);
}
