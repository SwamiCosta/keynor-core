package com.keynor.core.application.dto.event;

import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(
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
        boolean hidden,
        boolean common) {

    public static EventResponse from(Event event, List<EntityLinkSummary> links) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getSummary(),
                event.getBody(),
                event.getImages(),
                event.getCategories().stream().map(Enum::name).toList(),
                event.getStatus().name(),
                event.getTimeline() != null ? event.getTimeline().founded() : null,
                event.getTimeline() != null ? event.getTimeline().destroyed() : null,
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getLanguage().name(),
                event.getTranslationGroupId(),
                event.getVersionGroupId(),
                links.stream().map(LinkedEntityResponse::from).toList(),
                event.isHidden(),
                event.isCommon());
    }
}
