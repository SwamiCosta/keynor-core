package com.keynor.core.application.dto.place;

import com.keynor.core.domain.model.place.Place;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaceResponse(
        UUID id,
        String name,
        String summary,
        String body,
        List<String> tags,
        List<String> images,
        List<String> categories,
        String mapType,
        String status,
        String timelineFoundedEra,
        String timelineDestroyedEra,
        Instant createdAt,
        Instant updatedAt) {

    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getSummary(),
                place.getBody(),
                place.getTags(),
                place.getImages(),
                place.getCategories().stream().map(Enum::name).toList(),
                place.getMapType() != null ? place.getMapType().name() : null,
                place.getStatus().name(),
                place.getTimeline() != null ? place.getTimeline().founded() : null,
                place.getTimeline() != null ? place.getTimeline().destroyed() : null,
                place.getCreatedAt(),
                place.getUpdatedAt());
    }
}
