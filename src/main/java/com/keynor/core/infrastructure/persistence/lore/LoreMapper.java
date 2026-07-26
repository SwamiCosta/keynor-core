package com.keynor.core.infrastructure.persistence.lore;

import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class LoreMapper {

    private final EraRepository eraRepository;

    public LoreMapper(EraRepository eraRepository) {
        this.eraRepository = eraRepository;
    }

    public Lore toDomain(LoreEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(
                    resolveEraName(entity.getTimeline().getTimelineFoundedEraId()),
                    resolveEraName(entity.getTimeline().getTimelineDestroyedEraId()));
        }
        return new Lore(
                entity.getId(),
                entity.getName(),
                entity.getSummary(),
                entity.getBody(),
                entity.getImages() != null ? new ArrayList<>(entity.getImages()) : new ArrayList<>(),
                entity.getCategories() != null ? new ArrayList<>(entity.getCategories()) : new ArrayList<>(),
                entity.getStatus(),
                timeline,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLanguage(),
                entity.getTranslationGroupId(),
                entity.isHidden());
    }

    public LoreEntity toEntity(Lore lore) {
        LoreEntity entity = new LoreEntity();
        entity.setId(lore.getId());
        entity.setName(lore.getName());
        entity.setSummary(lore.getSummary());
        entity.setBody(lore.getBody());
        entity.setImages(new ArrayList<>(lore.getImages()));
        entity.setCategories(new ArrayList<>(lore.getCategories()));
        entity.setStatus(lore.getStatus());
        entity.setTimeline(toEmbeddable(lore));
        entity.setCreatedAt(lore.getCreatedAt());
        entity.setUpdatedAt(lore.getUpdatedAt());
        entity.setLanguage(lore.getLanguage());
        entity.setTranslationGroupId(lore.getTranslationGroupId());
        entity.setHidden(lore.isHidden());
        return entity;
    }

    private TimelineEmbeddable toEmbeddable(Lore lore) {
        if (lore.getTimeline() == null) return null;
        TimelineEmbeddable embeddable = new TimelineEmbeddable();
        embeddable.setTimelineFoundedEraId(resolveEraId(lore.getTimeline().founded()));
        embeddable.setTimelineDestroyedEraId(resolveEraId(lore.getTimeline().destroyed()));
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
