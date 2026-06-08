package com.keynor.core.application.dto.character;

import com.keynor.core.domain.model.character.Character;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CharacterResponse(
        UUID id,
        String name,
        String summary,
        String body,
        List<String> tags,
        List<String> images,
        List<String> categories,
        String status,
        String timelineFoundedEra,
        String timelineDestroyedEra,
        Instant createdAt,
        Instant updatedAt) {

    public static CharacterResponse from(Character character) {
        return new CharacterResponse(
                character.getId(),
                character.getName(),
                character.getSummary(),
                character.getBody(),
                character.getTags(),
                character.getImages(),
                character.getCategories().stream().map(Enum::name).toList(),
                character.getStatus().name(),
                character.getTimeline() != null ? character.getTimeline().founded() : null,
                character.getTimeline() != null ? character.getTimeline().destroyed() : null,
                character.getCreatedAt(),
                character.getUpdatedAt());
    }
}
