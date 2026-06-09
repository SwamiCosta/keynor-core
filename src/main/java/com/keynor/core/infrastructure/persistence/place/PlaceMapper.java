package com.keynor.core.infrastructure.persistence.place;

import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PlaceMapper {

    public Place toDomain(PlaceEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(entity.getTimeline().getTimelineFounded(), entity.getTimeline().getTimelineDestroyed());
        }
        return new Place(
                entity.getId(),
                entity.getName(),
                entity.getSummary(),
                entity.getBody(),
                entity.getTags() != null ? new ArrayList<>(entity.getTags()) : new ArrayList<>(),
                entity.getImages() != null ? new ArrayList<>(entity.getImages()) : new ArrayList<>(),
                entity.getCategories() != null ? new ArrayList<>(entity.getCategories()) : new ArrayList<>(),
                entity.getMapType(),
                entity.getStatus(),
                timeline,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public PlaceEntity toEntity(Place place) {
        PlaceEntity entity = new PlaceEntity();
        entity.setId(place.getId());
        entity.setName(place.getName());
        entity.setSummary(place.getSummary());
        entity.setBody(place.getBody());
        entity.setTags(new ArrayList<>(place.getTags()));
        entity.setImages(new ArrayList<>(place.getImages()));
        entity.setCategories(new ArrayList<>(place.getCategories()));
        entity.setMapType(place.getMapType());
        entity.setStatus(place.getStatus());
        entity.setTimeline(toEmbeddable(place));
        entity.setCreatedAt(place.getCreatedAt());
        entity.setUpdatedAt(place.getUpdatedAt());
        return entity;
    }

    private TimelineEmbeddable toEmbeddable(Place place) {
        if (place.getTimeline() == null) return null;
        TimelineEmbeddable embeddable = new TimelineEmbeddable();
        embeddable.setTimelineFounded(place.getTimeline().founded());
        embeddable.setTimelineDestroyed(place.getTimeline().destroyed());
        return embeddable;
    }
}
