package com.keynor.core.infrastructure.persistence.item;

import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ItemMapper {

    public Item toDomain(ItemEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(entity.getTimeline().getTimelineFounded(), entity.getTimeline().getTimelineDestroyed());
        }
        return new Item(
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

    public ItemEntity toEntity(Item item) {
        ItemEntity entity = new ItemEntity();
        entity.setId(item.getId());
        entity.setName(item.getName());
        entity.setSummary(item.getSummary());
        entity.setBody(item.getBody());
        entity.setTags(new ArrayList<>(item.getTags()));
        entity.setImages(new ArrayList<>(item.getImages()));
        entity.setCategories(new ArrayList<>(item.getCategories()));
        entity.setStatus(item.getStatus());
        entity.setTimeline(toEmbeddable(item));
        entity.setCreatedAt(item.getCreatedAt());
        entity.setUpdatedAt(item.getUpdatedAt());
        return entity;
    }

    private TimelineEmbeddable toEmbeddable(Item item) {
        if (item.getTimeline() == null) return null;
        TimelineEmbeddable embeddable = new TimelineEmbeddable();
        embeddable.setTimelineFounded(item.getTimeline().founded());
        embeddable.setTimelineDestroyed(item.getTimeline().destroyed());
        return embeddable;
    }
}
