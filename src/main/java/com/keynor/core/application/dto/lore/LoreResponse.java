package com.keynor.core.application.dto.lore;

import com.keynor.core.domain.model.lore.Lore;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LoreResponse(
        UUID id,
        String name,
        String summary,
        String body,
        List<String> tags,
        List<String> categories,
        String status,
        String timelineFoundedEra,
        String timelineDestroyedEra,
        Instant createdAt,
        Instant updatedAt) {

    public static LoreResponse from(Lore lore) {
        return new LoreResponse(
                lore.getId(),
                lore.getName(),
                lore.getSummary(),
                lore.getBody(),
                lore.getTags(),
                lore.getCategories().stream().map(Enum::name).toList(),
                lore.getStatus().name(),
                lore.getTimeline() != null ? lore.getTimeline().founded() : null,
                lore.getTimeline() != null ? lore.getTimeline().destroyed() : null,
                lore.getCreatedAt(),
                lore.getUpdatedAt());
    }
}
