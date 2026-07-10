package com.keynor.core.application.dto.place;

import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaceResponse(
        UUID id,
        String name,
        String summary,
        String body,
        List<String> images,
        List<String> categories,
        String mapType,
        String status,
        String timelineFoundedEra,
        String timelineDestroyedEra,
        Instant createdAt,
        Instant updatedAt,
        String language,
        UUID translationGroupId,
        List<LinkedEntityResponse> links) {

    public static PlaceResponse from(Place place, List<EntityLinkSummary> links) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getSummary(),
                place.getBody(),
                place.getImages(),
                place.getCategories().stream().map(Enum::name).toList(),
                place.getMapType() != null ? place.getMapType().name() : null,
                place.getStatus().name(),
                place.getTimeline() != null ? place.getTimeline().founded() : null,
                place.getTimeline() != null ? place.getTimeline().destroyed() : null,
                place.getCreatedAt(),
                place.getUpdatedAt(),
                place.getLanguage().name(),
                place.getTranslationGroupId(),
                links.stream().map(LinkedEntityResponse::from).toList());
    }
}
