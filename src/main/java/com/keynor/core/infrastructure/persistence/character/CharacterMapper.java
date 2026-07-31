package com.keynor.core.infrastructure.persistence.character;

import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class CharacterMapper {

    private final EraRepository eraRepository;

    public CharacterMapper(EraRepository eraRepository) {
        this.eraRepository = eraRepository;
    }

    public Character toDomain(CharacterEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(
                    resolveEraName(entity.getTimeline().getTimelineFoundedEraId()),
                    resolveEraName(entity.getTimeline().getTimelineDestroyedEraId()));
        }
        return new Character(
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
                entity.getVersionGroupId(),
                entity.isHidden());
    }

    public CharacterEntity toEntity(Character character) {
        CharacterEntity entity = new CharacterEntity();
        entity.setId(character.getId());
        entity.setName(character.getName());
        entity.setSummary(character.getSummary());
        entity.setBody(character.getBody());
        entity.setImages(new ArrayList<>(character.getImages()));
        entity.setCategories(new ArrayList<>(character.getCategories()));
        entity.setStatus(character.getStatus());
        entity.setTimeline(toEmbeddable(character));
        entity.setCreatedAt(character.getCreatedAt());
        entity.setUpdatedAt(character.getUpdatedAt());
        entity.setLanguage(character.getLanguage());
        entity.setTranslationGroupId(character.getTranslationGroupId());
        entity.setVersionGroupId(character.getVersionGroupId());
        entity.setHidden(character.isHidden());
        return entity;
    }

    private TimelineEmbeddable toEmbeddable(Character character) {
        if (character.getTimeline() == null) return null;
        TimelineEmbeddable embeddable = new TimelineEmbeddable();
        embeddable.setTimelineFoundedEraId(resolveEraId(character.getTimeline().founded()));
        embeddable.setTimelineDestroyedEraId(resolveEraId(character.getTimeline().destroyed()));
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
