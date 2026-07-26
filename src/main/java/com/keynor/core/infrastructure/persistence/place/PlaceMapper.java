package com.keynor.core.infrastructure.persistence.place;

import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class PlaceMapper {

    private final EraRepository eraRepository;

    public PlaceMapper(EraRepository eraRepository) {
        this.eraRepository = eraRepository;
    }

    public Place toDomain(PlaceEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(
                    resolveEraName(entity.getTimeline().getTimelineFoundedEraId()),
                    resolveEraName(entity.getTimeline().getTimelineDestroyedEraId()));
        }
        return new Place(
                entity.getId(),
                entity.getName(),
                entity.getSummary(),
                entity.getBody(),
                entity.getImages() != null ? new ArrayList<>(entity.getImages()) : new ArrayList<>(),
                entity.getCategories() != null ? new ArrayList<>(entity.getCategories()) : new ArrayList<>(),
                entity.getMapType(),
                entity.getStatus(),
                timeline,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLanguage(),
                entity.getTranslationGroupId(),
                entity.isHidden());
    }

    public PlaceEntity toEntity(Place place) {
        PlaceEntity entity = new PlaceEntity();
        entity.setId(place.getId());
        entity.setName(place.getName());
        entity.setSummary(place.getSummary());
        entity.setBody(place.getBody());
        entity.setImages(new ArrayList<>(place.getImages()));
        entity.setCategories(new ArrayList<>(place.getCategories()));
        entity.setMapType(place.getMapType());
        entity.setStatus(place.getStatus());
        entity.setTimeline(toEmbeddable(place));
        entity.setCreatedAt(place.getCreatedAt());
        entity.setUpdatedAt(place.getUpdatedAt());
        entity.setLanguage(place.getLanguage());
        entity.setTranslationGroupId(place.getTranslationGroupId());
        entity.setHidden(place.isHidden());
        return entity;
    }

    private TimelineEmbeddable toEmbeddable(Place place) {
        if (place.getTimeline() == null) return null;
        TimelineEmbeddable embeddable = new TimelineEmbeddable();
        embeddable.setTimelineFoundedEraId(resolveEraId(place.getTimeline().founded()));
        embeddable.setTimelineDestroyedEraId(resolveEraId(place.getTimeline().destroyed()));
        return embeddable;
    }

    private String resolveEraName(UUID eraId) {
        if (eraId == null) return null;
        return eraRepository.findById(eraId).map(Era::getName).orElse(null);
    }

    private UUID resolveEraId(String eraName) {
        if (eraName == null) return null;
        return eraRepository.findByName(eraName)
                .map(Era::getId)
                .orElseThrow(() -> new UnknownEraNameException(eraName));
    }
}
