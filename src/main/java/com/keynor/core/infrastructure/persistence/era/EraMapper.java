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
                entity.getUpdatedAt(),
                entity.getLanguage(),
                entity.getTranslationGroupId());
    }

    public EraEntity toEntity(Era era) {
        EraEntity entity = new EraEntity();
        entity.setId(era.getId());
        entity.setName(era.getName());
        entity.setOrderIndex(era.getOrderIndex());
        entity.setType(era.getType());
        entity.setImportance(era.getImportance());
        entity.setDescription(era.getDescription());
        entity.setCreatedAt(era.getCreatedAt());
        entity.setUpdatedAt(era.getUpdatedAt());
        entity.setLanguage(era.getLanguage());
        entity.setTranslationGroupId(era.getTranslationGroupId());
        return entity;
    }
}
