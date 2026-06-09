package com.keynor.core.infrastructure.persistence.character;

import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CharacterMapper {

    public Character toDomain(CharacterEntity entity) {
        Timeline timeline = null;
        if (entity.getTimeline() != null) {
            timeline = new Timeline(entity.getTimeline().getTimelineFounded(), entity.getTimeline().getTimelineDestroyed());
        }
        return new Character(
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

    public CharacterEntity toEntity(Character character) {
        CharacterEntity entity = new CharacterEntity();
        entity.setId(character.getId());
        entity.setName(character.getName());
        entity.setSummary(character.getSummary());
        entity.setBody(character.getBody());
        entity.setTags(new ArrayList<>(character.getTags()));
        entity.setImages(new ArrayList<>(character.getImages()));
        entity.setCategories(new ArrayList<>(character.getCategories()));
        entity.setStatus(character.getStatus());
        entity.setTimeline(toEmbeddable(character));
        entity.setCreatedAt(character.getCreatedAt());
        entity.setUpdatedAt(character.getUpdatedAt());
        return entity;
    }

    private TimelineEmbeddable toEmbeddable(Character character) {
        if (character.getTimeline() == null) return null;
        TimelineEmbeddable embeddable = new TimelineEmbeddable();
        embeddable.setTimelineFounded(character.getTimeline().founded());
        embeddable.setTimelineDestroyed(character.getTimeline().destroyed());
        return embeddable;
    }
}
