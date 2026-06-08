package com.keynor.core.infrastructure.persistence.faction;

import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FactionMapper {

    public Faction toDomain(FactionEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(entity.getTimeline().getTimelineFounded(), entity.getTimeline().getTimelineDestroyed());
        }
        return new Faction(
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

    public FactionEntity toEntity(Faction faction) {
        FactionEntity entity = new FactionEntity();
        entity.setId(faction.getId());
        entity.setName(faction.getName());
        entity.setSummary(faction.getSummary());
        entity.setBody(faction.getBody());
        entity.setTags(new ArrayList<>(faction.getTags()));
        entity.setImages(new ArrayList<>(faction.getImages()));
        entity.setCategories(new ArrayList<>(faction.getCategories()));
        entity.setStatus(faction.getStatus());
        entity.setTimeline(toEmbeddable(faction));
        entity.setCreatedAt(faction.getCreatedAt());
        entity.setUpdatedAt(faction.getUpdatedAt());
        return entity;
    }

    private TimelineEmbeddable toEmbeddable(Faction faction) {
        if (faction.getTimeline() == null) return null;
        TimelineEmbeddable embeddable = new TimelineEmbeddable();
        embeddable.setTimelineFounded(faction.getTimeline().founded());
        embeddable.setTimelineDestroyed(faction.getTimeline().destroyed());
        return embeddable;
    }
}
