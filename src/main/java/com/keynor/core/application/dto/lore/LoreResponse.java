package com.keynor.core.application.dto.lore;

import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LoreResponse(
        UUID id,
        String name,
        String summary,
        String body,
        List<String> images,
        List<String> categories,
        String status,
        String timelineFoundedEra,
        String timelineDestroyedEra,
        Instant createdAt,
        Instant updatedAt,
        String language,
        UUID translationGroupId,
        UUID versionGroupId,
        List<LinkedEntityResponse> links,
        boolean hidden) {

    public static LoreResponse from(Lore lore, List<EntityLinkSummary> links) {
        return new LoreResponse(
                lore.getId(),
                lore.getName(),
                lore.getSummary(),
                lore.getBody(),
                lore.getImages(),
                lore.getCategories().stream().map(Enum::name).toList(),
                lore.getStatus().name(),
                lore.getTimeline() != null ? lore.getTimeline().founded() : null,
                lore.getTimeline() != null ? lore.getTimeline().destroyed() : null,
                lore.getCreatedAt(),
                lore.getUpdatedAt(),
                lore.getLanguage().name(),
                lore.getTranslationGroupId(),
                lore.getVersionGroupId(),
                links.stream().map(LinkedEntityResponse::from).toList(),
                lore.isHidden());
    }
}
