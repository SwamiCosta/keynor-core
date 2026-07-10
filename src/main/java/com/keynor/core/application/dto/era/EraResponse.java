package com.keynor.core.application.dto.era;

import com.keynor.core.domain.model.era.Era;

import java.util.UUID;

public record EraResponse(
        UUID id,
        String name,
        int order,
        String type,
        String importance,
        String description,
        String language,
        UUID translationGroupId) {

    public static EraResponse from(Era era) {
        return new EraResponse(
                era.getId(),
                era.getName(),
                era.getOrderIndex(),
                era.getType().name(),
                era.getImportance() != null ? era.getImportance().name() : null,
                era.getDescription(),
                era.getLanguage().name(),
                era.getTranslationGroupId());
    }
}
