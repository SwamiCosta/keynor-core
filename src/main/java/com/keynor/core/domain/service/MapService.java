package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.map.GameMap;
import com.keynor.core.domain.port.in.map.FindAllMapsUseCase;
import com.keynor.core.domain.port.in.map.FindMapByIdUseCase;
import com.keynor.core.domain.port.in.map.FindMapsByEraUseCase;
import com.keynor.core.domain.port.out.MapRepository;

import java.util.List;

public class MapService implements FindAllMapsUseCase, FindMapByIdUseCase, FindMapsByEraUseCase {

    private final MapRepository mapRepository;

    public MapService(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    @Override
    public List<GameMap> findAll() {
        return mapRepository.findAll();
    }

    @Override
    public GameMap findById(String id) {
        return mapRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Map", id));
    }

    @Override
    public List<GameMap> findByEraId(String eraId) {
        return mapRepository.findByEraId(eraId);
    }
}
