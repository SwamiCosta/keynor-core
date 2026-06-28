package com.keynor.core.infrastructure.persistence.item;

import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class ItemMapper {

    private final EraRepository eraRepository;

    public ItemMapper(EraRepository eraRepository) {
        this.eraRepository = eraRepository;
    }

    public Item toDomain(ItemEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(
                    resolveEraName(entity.getTimeline().getTimelineFoundedEraId()),
                    resolveEraName(entity.getTimeline().getTimelineDestroyedEraId()));
        }
        return new Item(
                entity.getId(),
                entity.getName(),
                entity.getSummary(),
                entity.getBody(),
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
        embeddable.setTimelineFoundedEraId(resolveEraId(item.getTimeline().founded()));
        embeddable.setTimelineDestroyedEraId(resolveEraId(item.getTimeline().destroyed()));
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
