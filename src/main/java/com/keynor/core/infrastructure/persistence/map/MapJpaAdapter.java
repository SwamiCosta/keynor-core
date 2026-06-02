package com.keynor.core.infrastructure.persistence.map;

import com.keynor.core.domain.model.map.GameMap;
import com.keynor.core.domain.port.out.MapRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MapJpaAdapter implements MapRepository {

    private final MapJpaRepository jpaRepository;
    private final MapMapper mapper;

    public MapJpaAdapter(MapJpaRepository jpaRepository, MapMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<GameMap> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<GameMap> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<GameMap> findByEraId(String eraId) {
        return jpaRepository.findByEraId(eraId).stream().map(mapper::toDomain).toList();
    }
}
