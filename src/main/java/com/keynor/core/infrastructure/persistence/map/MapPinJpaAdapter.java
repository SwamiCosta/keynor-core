package com.keynor.core.infrastructure.persistence.map;

import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.out.MapPinRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MapPinJpaAdapter implements MapPinRepository {

    private final MapPinJpaRepository jpaRepository;
    private final MapPinMapper mapper;

    public MapPinJpaAdapter(MapPinJpaRepository jpaRepository, MapPinMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<MapPin> findByMapId(String mapId) {
        return jpaRepository.findByMapId(mapId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<MapPin> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByMapIdAndEntityTypeAndEntityId(String mapId, EntityType entityType, UUID entityId) {
        return jpaRepository.existsByMapIdAndEntityTypeAndEntityId(mapId, entityType, entityId);
    }

    @Override
    public MapPin save(MapPin pin) {
        MapPinEntity saved = jpaRepository.save(mapper.toEntity(pin));
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
