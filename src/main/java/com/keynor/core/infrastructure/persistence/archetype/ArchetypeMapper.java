package com.keynor.core.infrastructure.persistence.archetype;

import com.keynor.core.domain.model.archetype.Archetype;
import org.springframework.stereotype.Component;

@Component
public class ArchetypeMapper {

    public Archetype toDomain(ArchetypeEntity entity) {
        return new Archetype(
                entity.getId(),
                entity.getName(),
                entity.getElement(),
                entity.getSuit(),
                entity.getVocation(),
                entity.getTemperament(),
                entity.getCognitiveFunction(),
                entity.getSelfRelation(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
