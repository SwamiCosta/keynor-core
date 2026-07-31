package com.keynor.core.application.dto.character;

import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CharacterResponse(
        UUID id,
        String name,
        String summary,
        String body,
        List<String> images,
        List<String> categories,
        String status,
        String timelineFoundedEra,
        String timelineDestroyedEra,
        Instant createdAt,
        Instant updatedAt,
        String language,
        UUID translationGroupId,
        UUID versionGroupId,
        List<LinkedEntityResponse> links,
        boolean hidden) {

    public static CharacterResponse from(Character character, List<EntityLinkSummary> links) {
        return new CharacterResponse(
                character.getId(),
                character.getName(),
                character.getSummary(),
                character.getBody(),
                character.getImages(),
                character.getCategories().stream().map(Enum::name).toList(),
                character.getStatus().name(),
                character.getTimeline() != null ? character.getTimeline().founded() : null,
                character.getTimeline() != null ? character.getTimeline().destroyed() : null,
                character.getCreatedAt(),
                character.getUpdatedAt(),
                character.getLanguage().name(),
                character.getTranslationGroupId(),
                character.getVersionGroupId(),
                links.stream().map(LinkedEntityResponse::from).toList(),
                character.isHidden());
    }
}
