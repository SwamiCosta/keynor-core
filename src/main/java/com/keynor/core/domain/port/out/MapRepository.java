package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.map.GameMap;

import java.util.List;
import java.util.Optional;

public interface MapRepository {
    List<GameMap> findAll();
    Optional<GameMap> findById(String id);
    List<GameMap> findByEraId(String eraId);
}
