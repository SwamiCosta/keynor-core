package com.keynor.core.application.dto.item;

import com.keynor.core.domain.model.item.Item;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ItemResponse(
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

    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getSummary(),
                item.getBody(),
                item.getTags(),
                item.getCategories().stream().map(Enum::name).toList(),
                item.getStatus().name(),
                item.getTimeline() != null ? item.getTimeline().founded() : null,
                item.getTimeline() != null ? item.getTimeline().destroyed() : null,
                item.getCreatedAt(),
                item.getUpdatedAt());
    }
}
