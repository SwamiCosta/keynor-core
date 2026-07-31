package com.keynor.core.application.dto.item;

import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ItemResponse(
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

    public static ItemResponse from(Item item, List<EntityLinkSummary> links) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getSummary(),
                item.getBody(),
                item.getImages(),
                item.getCategories().stream().map(Enum::name).toList(),
                item.getStatus().name(),
                item.getTimeline() != null ? item.getTimeline().founded() : null,
                item.getTimeline() != null ? item.getTimeline().destroyed() : null,
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getLanguage().name(),
                item.getTranslationGroupId(),
                item.getVersionGroupId(),
                links.stream().map(LinkedEntityResponse::from).toList(),
                item.isHidden());
    }
}
