package com.keynor.core.infrastructure.persistence.lore;

import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class LoreMapper {

    public Lore toDomain(LoreEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(entity.getTimeline().getTimelineFounded(), entity.getTimeline().getTimelineDestroyed());
        }
        return new Lore(
                entity.getId(),
                entity.getName(),
                entity.getSummary(),
                entity.getBody(),
                entity.getTags() != null ? new ArrayList<>(entity.getTags()) : new ArrayList<>(),
                entity.getImages() != null ? new ArrayList<>(entity.getImages()) : new ArrayList<>(),
                entity.getCategories() != null ? new ArrayList<>(entity.getCategories()) : new ArrayList<>(),
                entity.getStatus(),
                timeline,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public LoreEntity toEntity(Lore lore) {
        LoreEntity entity = new LoreEntity();
        entity.setId(lore.getId());
        entity.setName(lore.getName());
        entity.setSummary(lore.getSummary());
        entity.setBody(lore.getBody());
        entity.setTags(new ArrayList<>(lore.getTags()));
        entity.setImages(new ArrayList<>(lore.getImages()));
        entity.setCategories(new ArrayList<>(lore.getCategories()));
        entity.setStatus(lore.getStatus());
        entity.setTimeline(toEmbeddable(lore));
        entity.setCreatedAt(lore.getCreatedAt());
        entity.setUpdatedAt(lore.getUpdatedAt());
        return entity;
    }

    private TimelineEmbeddable toEmbeddable(Lore lore) {
        if (lore.getTimeline() == null) return null;
        TimelineEmbeddable embeddable = new TimelineEmbeddable();
        embeddable.setTimelineFounded(lore.getTimeline().founded());
        embeddable.setTimelineDestroyed(lore.getTimeline().destroyed());
        return embeddable;
    }
}
