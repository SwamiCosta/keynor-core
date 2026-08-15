package com.keynor.core.infrastructure.persistence.map;

import com.keynor.core.domain.model.shared.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MapPinJpaRepository extends JpaRepository<MapPinEntity, UUID> {
    List<MapPinEntity> findByMapId(String mapId);
    boolean existsByMapIdAndEntityTypeAndEntityId(String mapId, EntityType entityType, UUID entityId);
    boolean existsByMapIdAndEntityTypeAndEntityIdAndIdNot(String mapId, EntityType entityType, UUID entityId, UUID excludedPinId);
}
