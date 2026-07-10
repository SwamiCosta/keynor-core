package com.keynor.core.infrastructure.persistence.sign;

import com.keynor.core.domain.model.sign.Sign;
import org.springframework.stereotype.Component;

@Component
public class SignMapper {

    public Sign toDomain(SignEntity entity) {
        return new Sign(
                entity.getId(),
                entity.getName(),
                entity.getSignOrder(),
                entity.getSeasonTime(),
                entity.getArchetypeId(),
                entity.getSubArchetype(),
                entity.getSummary(),
                entity.getBody(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLanguage(),
                entity.getTranslationGroupId());
    }
}
