package com.keynor.core.infrastructure.persistence.shared;

import com.keynor.core.domain.model.shared.EntityLink;
import org.springframework.stereotype.Component;

@Component
public class EntityLinkMapper {

    public EntityLink toDomain(EntityLinkEntity entity) {
        return new EntityLink(
                entity.getId(),
                entity.getSourceType(),
                entity.getSourceId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getCreatedAt());
    }
}
