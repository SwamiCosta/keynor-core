package com.keynor.core.application.dto.era;

import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.util.List;
import java.util.UUID;

public record EraResponse(
        UUID id,
        String name,
        int order,
        String type,
        String importance,
        String description,
        String language,
        UUID translationGroupId,
        List<LinkedEntityResponse> links) {

    public static EraResponse from(Era era, List<EntityLinkSummary> links) {
        return new EraResponse(
                era.getId(),
                era.getName(),
                era.getOrderIndex(),
                era.getType().name(),
                era.getImportance() != null ? era.getImportance().name() : null,
                era.getDescription(),
                era.getLanguage().name(),
                era.getTranslationGroupId(),
                links.stream().map(LinkedEntityResponse::from).toList());
    }
}
