package com.keynor.core.application.dto.faction;

import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FactionResponse(
        UUID id,
        String name,
        String summary,
        String body,
        List<String> images,
        List<String> categories,
        List<UUID> members,
        String status,
        String timelineFoundedEra,
        String timelineDestroyedEra,
        Instant createdAt,
        Instant updatedAt,
        String language,
        UUID translationGroupId,
        List<LinkedEntityResponse> links) {

    public static FactionResponse from(Faction faction, List<EntityLinkSummary> links) {
        return new FactionResponse(
                faction.getId(),
                faction.getName(),
                faction.getSummary(),
                faction.getBody(),
                faction.getImages(),
                faction.getCategories().stream().map(Enum::name).toList(),
                faction.getMembers(),
                faction.getStatus().name(),
                faction.getTimeline() != null ? faction.getTimeline().founded() : null,
                faction.getTimeline() != null ? faction.getTimeline().destroyed() : null,
                faction.getCreatedAt(),
                faction.getUpdatedAt(),
                faction.getLanguage().name(),
                faction.getTranslationGroupId(),
                links.stream().map(LinkedEntityResponse::from).toList());
    }
}
