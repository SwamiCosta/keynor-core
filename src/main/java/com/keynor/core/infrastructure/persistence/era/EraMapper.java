package com.keynor.core.infrastructure.persistence.era;

import com.keynor.core.domain.model.era.Era;
import org.springframework.stereotype.Component;

@Component
public class EraMapper {

    public Era toDomain(EraEntity entity) {
        return new Era(
                entity.getId(),
                entity.getName(),
                entity.getEraOrder(),
                entity.getPeriod(),
                entity.getSummary(),
                entity.getMapType(),
                entity.getDefaultMap(),
                entity.getColor());
    }
}
