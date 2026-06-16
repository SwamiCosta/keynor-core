package com.keynor.core.infrastructure.persistence.shared;

import com.keynor.core.domain.model.shared.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EntityLinkJpaRepository extends JpaRepository<EntityLinkEntity, UUID> {

    List<EntityLinkEntity> findBySourceTypeAndSourceId(EntityType sourceType, UUID sourceId);

    void deleteBySourceTypeAndSourceId(EntityType sourceType, UUID sourceId);

    void deleteBySourceTypeAndSourceIdOrTargetTypeAndTargetId(
            EntityType sourceType, UUID sourceId, EntityType targetType, UUID targetId);
}
