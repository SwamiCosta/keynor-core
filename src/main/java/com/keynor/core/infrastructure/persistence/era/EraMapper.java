package com.keynor.core.infrastructure.persistence.era;

import com.keynor.core.domain.model.era.Era;
import org.springframework.stereotype.Component;

@Component
public class EraMapper {

    public Era toDomain(EraEntity entity) {
        return new Era(
                entity.getId(),
                entity.getName(),
                entity.getOrderIndex(),
                entity.getType(),
                entity.getImportance(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
