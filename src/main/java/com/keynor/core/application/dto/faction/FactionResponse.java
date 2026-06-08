package com.keynor.core.application.dto.faction;

import com.keynor.core.domain.model.faction.Faction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FactionResponse(
        UUID id,
        String name,
        String summary,
        String body,
        List<String> tags,
        List<String> images,
        List<String> categories,
        String status,
        String timelineFoundedEra,
        String timelineDestroyedEra,
        Instant createdAt,
        Instant updatedAt) {

    public static FactionResponse from(Faction faction) {
        return new FactionResponse(
                faction.getId(),
                faction.getName(),
                faction.getSummary(),
                faction.getBody(),
                faction.getTags(),
                faction.getImages(),
                faction.getCategories().stream().map(Enum::name).toList(),
                faction.getStatus().name(),
                faction.getTimeline() != null ? faction.getTimeline().founded() : null,
                faction.getTimeline() != null ? faction.getTimeline().destroyed() : null,
                faction.getCreatedAt(),
                faction.getUpdatedAt());
    }
}
