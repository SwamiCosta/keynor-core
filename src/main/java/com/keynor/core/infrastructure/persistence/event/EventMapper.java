package com.keynor.core.infrastructure.persistence.event;

import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class EventMapper {

    public Event toDomain(EventEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(entity.getTimeline().getTimelineFounded(), entity.getTimeline().getTimelineDestroyed());
        }
        return new Event(
                entity.getId(),
                entity.getName(),
                entity.getSummary(),
                entity.getBody(),
                entity.getTags() != null ? new ArrayList<>(entity.getTags()) : new ArrayList<>(),
                entity.getCategories() != null ? new ArrayList<>(entity.getCategories()) : new ArrayList<>(),
                entity.getStatus(),
                timeline,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public EventEntity toEntity(Event event) {
        EventEntity entity = new EventEntity();
        entity.setId(event.getId());
        entity.setName(event.getName());
        entity.setSummary(event.getSummary());
        entity.setBody(event.getBody());
        entity.setTags(new ArrayList<>(event.getTags()));
        entity.setCategories(new ArrayList<>(event.getCategories()));
        entity.setStatus(event.getStatus());
        entity.setTimeline(toEmbeddable(event));
        entity.setCreatedAt(event.getCreatedAt());
        entity.setUpdatedAt(event.getUpdatedAt());
        return entity;
    }

    private TimelineEmbeddable toEmbeddable(Event event) {
        if (event.getTimeline() == null) return null;
        TimelineEmbeddable embeddable = new TimelineEmbeddable();
        embeddable.setTimelineFounded(event.getTimeline().founded());
        embeddable.setTimelineDestroyed(event.getTimeline().destroyed());
        return embeddable;
    }
}
